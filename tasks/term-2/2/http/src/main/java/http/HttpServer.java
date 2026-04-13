package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HttpServer {

    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());
    private static final int SHUTDOWN_TIMEOUT_S = 5;
    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;

    private final Map<String, Function<HttpRequest, HttpResponse>> handlers;

    private volatile boolean running;
    private ServerSocketChannel serverChannel;
    private ExecutorService workerPool;
    private Thread acceptThread;

    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;
        this.handlers = new HashMap<>();
    }

    public String  getHost() { return host; }
    public int getPort() { return port; }
    public int getThreadCount() { return threadCount; }
    public boolean isVirtual() { return isVirtual; }

    public void addHandler(HttpMethod method, String path, Function<HttpRequest, HttpResponse> handler) {
        handlers.put(routeKey(method, path), handler);
    }

    public Function<HttpRequest, HttpResponse> getHandler(HttpMethod method, String path) {
        return handlers.get(routeKey(method, path));
    }

    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        workerPool = isVirtual ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(threadCount);

        serverChannel = ServerSocketChannel.open();
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverChannel.configureBlocking(true);
        serverChannel.bind(new InetSocketAddress(host, port));

        running = true;

        acceptThread = new Thread(this::acceptLoop, "http-accept-thread");
        acceptThread.setDaemon(true);
        acceptThread.start();

        LOGGER.info(String.format("HTTP server started on %s:%d  [threads=%d, virtual=%b]",
                host, port, threadCount, isVirtual));
    }

    public void stop() throws IOException, InterruptedException {
        if (!running) return;
        running = false;

        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }

        if (workerPool != null) {
            workerPool.shutdown();
            if (!workerPool.awaitTermination(SHUTDOWN_TIMEOUT_S, TimeUnit.SECONDS)) {
                LOGGER.warning("Worker pool did not terminate in time; forcing shutdown");
                workerPool.shutdownNow();
            }
        }

        if (acceptThread != null) {
            acceptThread.join(SHUTDOWN_TIMEOUT_S * 1000L);
        }

        LOGGER.info("HTTP server stopped");
    }

    private void acceptLoop() {
        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    workerPool.submit(() -> handleClient(clientChannel));
                }
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        }
    }

    private void handleClient(SocketChannel channel) {
        try (channel) {
            HttpRequest request = HttpRequestParser.parse(channel);

            Function<HttpRequest, HttpResponse> handler =
                    getHandler(request.getMethod(), request.getPath());

            HttpResponse response;
            if (handler != null) {
                response = invokeHandler(handler, request);
            } else {
                response = HttpResponse.notFound(
                        "No handler registered for "
                                + request.getMethod() + " " + request.getPath());
            }

            HttpResponseWriter.write(channel, response);

        } catch (EmptyRequestException e) {

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error processing client request", e);
        }
    }

    private HttpResponse invokeHandler(Function<HttpRequest, HttpResponse> handler,
                                       HttpRequest request) {
        try {
            return handler.apply(request);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Handler threw an unhandled exception", e);
            return HttpResponse.internalServerError("Internal Server Error");
        }
    }

    private String routeKey(HttpMethod method, String path) {
        return method.name() + ":" + path;
    }
}