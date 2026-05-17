package http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

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
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        if (!isVirtual && threads <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.isVirtual = isVirtual;
    }

    public HttpServer on(HttpMethod method, String path, RouteHandler handler) {
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

        System.out.println("Server started on " + host + ":" + port
                + " (threads=" + (isVirtual ? "virtual" : threads) + ")");
    }

    public void stop() throws IOException {
        running = false;
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } finally {
            if (pool != null) {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                        pool.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("Server stopped.");
    }

    private void acceptLoop() {
        while (running) {
            SocketChannel client;
            try {
                client = serverChannel.accept();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
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
            InputStream in = channel.socket().getInputStream();
            OutputStream out = channel.socket().getOutputStream();

            HttpRequest request;
            try {
                request = RequestParser.parse(in);
            } catch (IOException parseErr) {
                ResponseWriter.write(out, new HttpResponse().status(400).writeText("Bad Request"));
                return;
            }

            HttpResponse response = new HttpResponse();
            RouteHandler handler = lookup(request.method(), request.path());

            if (handler == null) {
                response.status(404).writeText("Not Found");
            } else {
                try {
                    handler.handle(request, response);
                } catch (Exception userErr) {
                    userErr.printStackTrace();
                    response = new HttpResponse().status(500).writeText("Internal Server Error");
                }
            }

            ResponseWriter.write(out, response);
        } catch (IOException io) {
            // The client may close the connection before the response is written.
        }
    }

    private RouteHandler lookup(HttpMethod method, String path) {
        Map<String, RouteHandler> byPath = routes.get(method);
        if (byPath == null) {
            return null;
        }
        return byPath.get(path);
    }

    public Map<HttpMethod, Map<String, RouteHandler>> routes() {
        Map<HttpMethod, Map<String, RouteHandler>> snapshot = new HashMap<>();
        for (Map.Entry<HttpMethod, Map<String, RouteHandler>> e : routes.entrySet()) {
            snapshot.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return snapshot;
    }
}
