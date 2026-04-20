package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final String host;
    private final int port;
    private final ExecutorService executor;
    private final Map<String, Map<HttpMethod, HttpHandler>> routes = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private ServerSocketChannel serverChannel;
    private Thread acceptThread;

    public HttpServer(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        if (isVirtual) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            this.executor = Executors.newFixedThreadPool(threads);
        }
    }

    public void addRoute(HttpMethod method, String path, HttpHandler handler) {
        routes.computeIfAbsent(path, k -> new ConcurrentHashMap<>()).put(method, handler);
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        running = true;
        acceptThread = Thread.ofPlatform().name("http-acceptor").unstarted(this::acceptLoop);
        acceptThread.start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                SocketChannel client = serverChannel.accept();
                executor.submit(() -> handleClient(client));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept error: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(SocketChannel client) {
        try {
            client.configureBlocking(true);
            HttpRequest request = RequestParser.parse(client);
            HttpHandler handler = findHandler(request);
            HttpResponse response;
            if (handler != null) {
                try {
                    response = handler.handle(request);
                } catch (Exception e) {
                    e.printStackTrace();
                    response = new HttpResponse(500, "Internal Server Error");
                }
            } else {
                response = new HttpResponse(404, "Not Found");
            }
            sendResponse(client, response);
        } catch (Exception e) {
            if (!(e instanceof ClosedChannelException)) {
                e.printStackTrace();
            }
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private HttpHandler findHandler(HttpRequest request) {
        Map<HttpMethod, HttpHandler> methodMap = routes.get(request.getPath());
        if (methodMap != null) {
            return methodMap.get(request.getMethod());
        }
        return null;
    }

    private void sendResponse(SocketChannel client, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ")
                .append(getStatusText(response.getStatusCode())).append("\r\n");

        if (!response.getHeaders().containsKey("Content-Length")) {
            response.addHeader("Content-Length", String.valueOf(response.getBody().length));
        }
        if (!response.getHeaders().containsKey("Connection")) {
            response.addHeader("Connection", "close");
        }

        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + response.getBody().length);
        buffer.put(headerBytes);
        buffer.put(response.getBody());
        buffer.flip();

        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
    }

    private String getStatusText(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    public void stop() {
        running = false;
        try { if (serverChannel != null) serverChannel.close(); } catch (IOException ignored) {}
        if (acceptThread != null) acceptThread.interrupt();
        executor.shutdown();
    }
}