package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    private final Map<String, HttpHandler> routes = new HashMap<>();

    private volatile boolean running;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;

    public HttpServer(int port, int threadCount, boolean isVirtual) {
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;
    }

    public void addRoute(HttpMethod method, String path, HttpHandler handler) {
        routes.put(method.name() + " " + path, handler);
    }

    public void start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(true);

            if (isVirtual) {
                executor = Executors.newVirtualThreadPerTaskExecutor();
            } else {
                executor = Executors.newFixedThreadPool(threadCount);
            }

            running = true;
            System.out.println("Server started on port " + port
                    + " (" + (isVirtual ? "virtual" : threadCount + " platform") + " threads)");

            while (running) {
                SocketChannel client = serverChannel.accept();
                executor.submit(() -> handleClient(client));
            }
        } catch (IOException e) {
            if (running) {
                throw new RuntimeException("Server error", e);
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            System.err.println("Error closing server channel: " + e.getMessage());
        }
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            HttpRequest request = RequestParser.parse(client);
            if (request == null) return;

            HttpResponse response = new HttpResponse();
            String routeKey = request.getMethod().name() + " " + request.getPath();
            HttpHandler handler = routes.get(routeKey);

            if (handler != null) {
                handler.handle(request, response);
            } else {
                response.setStatus(404);
                response.setBody("Not Found");
            }

            client.write(ByteBuffer.wrap(response.toBytes()));
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
