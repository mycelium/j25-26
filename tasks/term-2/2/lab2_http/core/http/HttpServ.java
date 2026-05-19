package core.http;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class HttpServ {
    private final int port;
    private final String host;
    private final ExecutorService executor;
    private final Map<String, Map<String, HttpHandler>> routes = new HashMap<>();
    private ServerSocketChannel serverChannel;

    public interface HttpHandler {
        void handle(HttpReq request, HttpRes response);
    }

    public HttpServ(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;

        if (isVirtual) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            this.executor = Executors.newFixedThreadPool(threads);
        }
    }

    public void addListener(String method, String path, HttpHandler handler) {
        routes.computeIfAbsent(path, _ -> new HashMap<>())
              .put(method.toUpperCase(), handler);
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        System.out.println("Server started on http://" + host + ":" + port);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                executor.submit(() -> processRequest(clientChannel));
            } catch (ClosedChannelException e) {
                break;
            }
        }
    }

    public void stop() throws IOException {
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server channel: " + e.getMessage());
        }

        if (executor != null) {
            executor.shutdown(); 
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); 
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Server stopped");
    }

    private void processRequest(SocketChannel client) {
        try (client;
             InputStream is = Channels.newInputStream(client);
             OutputStream os = Channels.newOutputStream(client)) {

            HttpReq request = new HttpReq(is);
            HttpRes response = new HttpRes();

            HttpHandler handler = routes
                    .getOrDefault(request.getPath(), Collections.emptyMap())
                    .get(request.getMethod());

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    System.err.println("Handler error: " + e.getMessage());
                    response = new HttpRes();
                    response.setStatus(500); 
                    response.setBody("500 Internal Server Error: " + e.getMessage());
                }
            } else {
                response.setStatus(404); 
                response.setBody("404 Page Not Found");
            }

            response.send(os);

        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}