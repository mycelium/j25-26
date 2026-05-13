package org.api;

import org.internal.HttpRequestParser;
import org.internal.HttpResponseWriter;
import org.internal.Router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HttpServer {

    private static final Logger log = Logger.getLogger(HttpServer.class.getName());

    private final ServerConfig  config;
    private final Router        router  = new Router();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocketChannel serverChannel;
    private ExecutorService     executor;
    private Thread              acceptThread;

    public HttpServer(ServerConfig config) {
        this.config = config;
    }

    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this(new ServerConfig(host, port, threadCount, isVirtual));
    }

    public void get(String path, RequestHandler handler)    { router.add(HttpMethod.GET,    path, handler); }
    public void post(String path, RequestHandler handler)   { router.add(HttpMethod.POST,   path, handler); }
    public void put(String path, RequestHandler handler)    { router.add(HttpMethod.PUT,    path, handler); }
    public void patch(String path, RequestHandler handler)  { router.add(HttpMethod.PATCH,  path, handler); }
    public void delete(String path, RequestHandler handler) { router.add(HttpMethod.DELETE, path, handler); }

    public void addRoute(HttpMethod method, String path, RequestHandler handler) {
        router.add(method, path, handler);
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true))
            throw new IllegalStateException("Server is already running");

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(true);
        serverChannel.socket().setReuseAddress(true);
        serverChannel.bind(new InetSocketAddress(config.host(), config.port()));

        executor = config.isVirtual()
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(config.threadCount());

        acceptThread = Thread.ofPlatform()
                .name("http-accept")
                .daemon(true)
                .start(this::acceptLoop);

        log.info("HttpServer started on " + config.host() + ":" + config.port()
                + " [" + (config.isVirtual() ? "virtual" : "platform x" + config.threadCount()) + "]");
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) return;

        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "Error closing server channel", e);
        }

        if (acceptThread != null) {
            try { acceptThread.join(2000); } catch (InterruptedException ignored) {}
        }

        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS))
                    executor.shutdownNow();
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("HttpServer stopped");
    }

    public boolean isRunning() { return running.get(); }

    private void acceptLoop() {
        while (running.get()) {
            try {
                SocketChannel client = serverChannel.accept();
                if (client == null) continue;
                executor.submit(() -> handleClient(client));
            } catch (AsynchronousCloseException e) {
                break;
            } catch (IOException e) {
                if (running.get()) log.log(Level.WARNING, "Accept error", e);
            }
        }
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            HttpRequest request = HttpRequestParser.parse(client);
            if (request == null) return;

            HttpResponse response = router.dispatch(request);
            HttpResponseWriter.write(client, response);
        } catch (IOException e) {
            log.log(Level.FINE, "Client I/O error", e);
        }
    }
}
