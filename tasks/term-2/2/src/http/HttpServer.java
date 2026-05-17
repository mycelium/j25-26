package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {

    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());
    private static final long STOP_TIMEOUT_MILLIS = 5000;

    private final String host;
    private final int port;
    private final int threads;
    private final boolean isVirtual;

    private final Map<HttpMethod, Map<String, RouteHandler>> routes = new ConcurrentHashMap<>();

    private ServerSocketChannel serverChannel;
    private ExecutorService pool;
    private volatile boolean running;
    private Thread acceptThread;

    public HttpServer(String host, int port, int threads, boolean isVirtual) {
        validate(host, port, threads, isVirtual);
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.isVirtual = isVirtual;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HttpServer on(HttpMethod method, String path, RouteHandler handler) {
        Objects.requireNonNull(method, "Method must not be null");
        Objects.requireNonNull(handler, "Handler must not be null");
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with /");
        }
        routes.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public void start() throws IOException {
        if (running) {
            return;
        }

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(true);
        serverChannel.bind(new InetSocketAddress(host, port));

        pool = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threads);

        running = true;

        acceptThread = new Thread(this::acceptLoop, "http-accept");
        acceptThread.setDaemon(false);
        acceptThread.start();
    }

    public void stop() throws IOException {
        running = false;
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
            joinAcceptThread();
        } finally {
            shutdownPool();
        }
    }

    public Map<HttpMethod, Map<String, RouteHandler>> routes() {
        Map<HttpMethod, Map<String, RouteHandler>> snapshot = new HashMap<>();
        for (Map.Entry<HttpMethod, Map<String, RouteHandler>> e : routes.entrySet()) {
            snapshot.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return snapshot;
    }

    private void acceptLoop() {
        while (running) {
            SocketChannel client;
            try {
                client = serverChannel.accept();
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Cannot accept client connection", e);
                }
                continue;
            }
            if (client == null) {
                continue;
            }

            final SocketChannel conn = client;
            pool.submit(() -> processConnection(conn));
        }
    }

    private void processConnection(SocketChannel channel) {
        try (channel) {
            channel.configureBlocking(true);

            HttpRequest request;
            try {
                request = RequestParser.parse(channel);
            } catch (IOException parseErr) {
                ResponseWriter.write(channel, new HttpResponse().status(400).writeText("Bad Request"));
                return;
            }

            HttpResponse response = new HttpResponse();
            RouteHandler handler = lookup(request.method(), request.path());

            if (handler == null) {
                writeMissingRouteResponse(request, response);
            } else {
                try {
                    handler.handle(request, response);
                } catch (Exception userErr) {
                    LOGGER.log(Level.WARNING, "Route handler failed", userErr);
                    response = new HttpResponse().status(500).writeText("Internal Server Error");
                }
            }

            ResponseWriter.write(channel, response);
        } catch (IOException io) {
            LOGGER.log(Level.FINE, "Client connection closed before response was written", io);
        }
    }

    private void writeMissingRouteResponse(HttpRequest request, HttpResponse response) {
        if (pathExists(request.path())) {
            response.status(405)
                    .header("Allow", allowedMethods(request.path()))
                    .writeText("Method Not Allowed");
        } else {
            response.status(404).writeText("Not Found");
        }
    }

    private RouteHandler lookup(HttpMethod method, String path) {
        Map<String, RouteHandler> byPath = routes.get(method);
        if (byPath == null) {
            return null;
        }
        return byPath.get(path);
    }

    private boolean pathExists(String path) {
        for (Map<String, RouteHandler> byPath : routes.values()) {
            if (byPath.containsKey(path)) {
                return true;
            }
        }
        return false;
    }

    private String allowedMethods(String path) {
        StringJoiner joiner = new StringJoiner(", ");
        for (HttpMethod method : HttpMethod.values()) {
            Map<String, RouteHandler> byPath = routes.get(method);
            if (byPath != null && byPath.containsKey(path)) {
                joiner.add(method.name());
            }
        }
        return joiner.toString();
    }

    private void joinAcceptThread() {
        if (acceptThread == null || Thread.currentThread() == acceptThread) {
            return;
        }
        try {
            acceptThread.join(STOP_TIMEOUT_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownPool() {
        if (pool == null) {
            return;
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(STOP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException exception) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void validate(String host, int port, int threads, boolean isVirtual) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        if (!isVirtual && threads <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
    }

    public static final class Builder {
        private String host = "localhost";
        private int port = 8080;
        private int threads = Runtime.getRuntime().availableProcessors();
        private boolean virtual;

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder virtual(boolean virtual) {
            this.virtual = virtual;
            return this;
        }

        public HttpServer build() {
            return new HttpServer(host, port, threads, virtual);
        }
    }
}
