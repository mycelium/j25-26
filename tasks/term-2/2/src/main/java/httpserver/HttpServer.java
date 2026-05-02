package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lightweight HTTP/1.1 server built on Java NIO ({@link ServerSocketChannel}).
 * No external libraries are used.
 *
 * <h2>Quick start</h2>
 * <pre>{@code
 * HttpServer server = new HttpServer.Builder()
 *         .host("localhost")
 *         .port(8080)
 *         .threadCount(4)
 *         .isVirtual(false)
 *         .build();
 *
 * server.addRoute(HttpMethod.GET, "/hello", (req, res) ->
 *         res.body("Hello, World!"));
 *
 * server.start();
 * }</pre>
 */
public class HttpServer {

    private static final Logger LOG = Logger.getLogger(HttpServer.class.getName());

    // ── Nested: handler interface ──────────────────────────────────────────────

    /**
     * Route handler — implement this (or pass a lambda) to process requests.
     *
     * <pre>{@code
     * server.addRoute(HttpMethod.POST, "/echo", (req, res) -> {
     *     res.json("{\"body\":\"" + req.getBody() + "\"}");
     * });
     * }</pre>
     */
    @FunctionalInterface
    public interface Handler {
        /**
         * @param request  the parsed incoming request
         * @param response the response to populate
         * @throws Exception any error; the server will return a 500 response
         */
        void handle(HttpRequest request, HttpResponse response) throws Exception;
    }

    // ── Configuration ──────────────────────────────────────────────────────────

    private final String  host;
    private final int     port;
    private final int     threadCount;
    private final boolean isVirtual;

    // ── State ──────────────────────────────────────────────────────────────────

    private final Map<RouteKey, Handler> routes = new ConcurrentHashMap<>();
    private ExecutorService   executor;
    private ServerSocketChannel serverChannel;
    private Selector          selector;
    private volatile boolean  running = false;
    private Thread            selectorThread;

    private HttpServer(Builder b) {
        this.host        = b.host;
        this.port        = b.port;
        this.threadCount = b.threadCount;
        this.isVirtual   = b.isVirtual;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Register a handler for the given HTTP method and path.
     *
     * @param method  HTTP method
     * @param path    exact path to match (e.g. {@code "/api/users"})
     * @param handler lambda or implementation of {@link Handler}
     */
    public void addRoute(HttpMethod method, String path, Handler handler) {
        routes.put(new RouteKey(method, path), handler);
    }

    /**
     * Start the server. Non-blocking — the selector loop runs on a daemon thread.
     *
     * @throws IOException if the socket cannot be opened / bound
     */
    public void start() throws IOException {
        executor      = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threadCount, r -> {
                    Thread t = new Thread(r, "http-worker");
                    t.setDaemon(true);
                    return t;
                  });

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(host, port));

        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        selectorThread = new Thread(this::loop, "http-selector");
        selectorThread.setDaemon(true);
        selectorThread.start();

        LOG.info(() -> String.format(
                "HttpServer started on %s:%d  [threads=%d, virtual=%b]",
                host, port, threadCount, isVirtual));
    }

    /** Graceful shutdown — waits up to 5 s for in-flight requests to finish. */
    public void stop() {
        running = false;
        if (selector != null) selector.wakeup();
        try { if (selectorThread != null) selectorThread.join(3_000); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        shutdownExecutor();
        closeQuietly();
        LOG.info("HttpServer stopped.");
    }

    /** Block the calling thread until {@link #stop()} is called. */
    public void awaitTermination() {
        try { if (selectorThread != null) selectorThread.join(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Selector loop ──────────────────────────────────────────────────────────

    private void loop() {
        while (running) {
            try {
                selector.select(500);
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isValid() && key.isAcceptable()) accept();
                }
            } catch (IOException e) {
                if (running) LOG.log(Level.WARNING, "Selector error", e);
            }
        }
    }

    private void accept() {
        try {
            SocketChannel client = serverChannel.accept();
            if (client == null) return;
            client.configureBlocking(true);
            executor.submit(new HttpRequestParser(client, routes));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to accept connection", e);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void shutdownExecutor() {
        if (executor == null) return;
        executor.shutdown();
        try { if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow(); }
        catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
    }

    private void closeQuietly() {
        try { if (serverChannel != null) serverChannel.close(); } catch (IOException ignored) {}
        try { if (selector      != null) selector.close();      } catch (IOException ignored) {}
    }

    // ── Builder ────────────────────────────────────────────────────────────────

    public static class Builder {
        private String  host        = "localhost";
        private int     port        = 8080;
        private int     threadCount = Runtime.getRuntime().availableProcessors();
        private boolean isVirtual   = false;

        /** Hostname / IP to listen on. Default: {@code localhost} */
        public Builder host(String host)           { this.host = host;               return this; }

        /** Port to listen on. Default: {@code 8080} */
        public Builder port(int port)              { this.port = port;               return this; }

        /**
         * Number of worker threads in the fixed pool.
         * Ignored when {@link #isVirtual} is {@code true}.
         * Default: number of available CPU cores.
         */
        public Builder threadCount(int n)          { this.threadCount = n;           return this; }

        /**
         * {@code true}  → one Java 21 virtual thread per connection.<br>
         * {@code false} → fixed thread pool of size {@link #threadCount}.
         */
        public Builder isVirtual(boolean isVirtual){ this.isVirtual = isVirtual;     return this; }

        public HttpServer build() {
            if (port < 1 || port > 65_535)  throw new IllegalArgumentException("Invalid port: " + port);
            if (threadCount < 1)            throw new IllegalArgumentException("threadCount must be >= 1");
            return new HttpServer(this);
        }
    }
}
