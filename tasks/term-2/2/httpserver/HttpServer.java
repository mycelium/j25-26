package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;

    private final Map<String, HttpHandler> routes = new HashMap<>();

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running = false;

    /**
     * @param host        hostname or IP to bind (e.g. "localhost" or "0.0.0.0")
     * @param port        TCP port to listen on
     * @param threadCount number of worker threads in the pool
     * @param isVirtual   if {@code true}, use virtual threads (Java 21+); otherwise platform threads
     */
    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;
    }

    /**
     * Register a handler for a specific HTTP method and path.
     *
     * @param method  HTTP method (GET, POST, …)
     * @param path    exact path to match (e.g. "/api/users")
     * @param handler callback that fills in the response
     */
    public void addRoute(HttpMethod method, String path, HttpHandler handler) {
        String key = routeKey(method, path);
        routes.put(key, handler);
    }

    public void addRoute(String method, String path, HttpHandler handler) {
        addRoute(HttpMethod.valueOf(method.toUpperCase()), path, handler);
    }

    /**
     * Start the server. This method blocks; run it on a separate thread if needed.
     *
     * @throws IOException if the server socket cannot be opened
     */
    public void start() throws IOException {
        executor = buildExecutor();

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        running = true;
        System.out.println("HttpServer started on " + host + ":" + port +
                " [threads=" + threadCount + ", virtual=" + isVirtual + "]");

        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    executor.submit(() -> handleClient(clientChannel));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server channel: " + e.getMessage());
        }
        if (executor != null) {
            executor.shutdown();
        }
        System.out.println("HttpServer stopped.");
    }

    private ExecutorService buildExecutor() {
        if (isVirtual) {
            return Executors.newVirtualThreadPerTaskExecutor();
        } else {
            return Executors.newFixedThreadPool(threadCount);
        }
    }

    private void handleClient(SocketChannel channel) {
        try (SocketChannel ch = channel) {
            byte[] raw = readRequest(ch);
            if (raw == null || raw.length == 0) return;

            HttpRequest request;
            try {
                request = RequestParser.parse(raw, raw.length);
            } catch (IOException e) {
                sendError(ch, 400, "Bad Request", e.getMessage());
                return;
            }

            HttpResponse response = new HttpResponse();

            String key = routeKey(request.getMethod(), request.getPath());
            HttpHandler handler = routes.get(key);

            if (handler == null) {
                response.setStatus(404).setBody("404 Not Found: " + request.getPath());
            } else {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    response.setStatus(500).setBody("500 Internal Server Error: " + e.getMessage());
                    System.err.println("Handler threw exception: " + e.getMessage());
                }
            }

            writeResponse(ch, response);

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }


    private byte[] readRequest(SocketChannel ch) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
        ByteBuffer nio = ByteBuffer.allocate(4096);

        int headerEnd = -1;
        int contentLength = -1;

        while (true) {
            nio.clear();
            int bytesRead = ch.read(nio);
            if (bytesRead <= 0) break;

            nio.flip();
            byte[] chunk = new byte[nio.remaining()];
            nio.get(chunk);
            buffer.write(chunk);

            byte[] current = buffer.toByteArray();

            if (headerEnd == -1) {
                headerEnd = indexOf(current, "\r\n\r\n");
                if (headerEnd >= 0) {
                    String headerSection = new String(current, 0, headerEnd);
                    contentLength = parseContentLength(headerSection);
                    if (contentLength == 0) break; 
                }
            }

            if (headerEnd >= 0) {
                int bodyRead = current.length - (headerEnd + 4);
                if (contentLength < 0 || bodyRead >= contentLength) {
                    break; 
                }
            }
        }

        return buffer.toByteArray();
    }

    private int parseContentLength(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    return Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private int indexOf(byte[] data, String pattern) {
        byte[] p = pattern.getBytes();
        outer:
        for (int i = 0; i <= data.length - p.length; i++) {
            for (int j = 0; j < p.length; j++) {
                if (data[i + j] != p[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private void writeResponse(SocketChannel ch, HttpResponse response) throws IOException {
        byte[] bytes = response.toBytes();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    private void sendError(SocketChannel ch, int code, String msg, String detail) {
        HttpResponse r = new HttpResponse();
        r.setStatus(code, msg).setBody(code + " " + msg + (detail != null ? ": " + detail : ""));
        try {
            writeResponse(ch, r);
        } catch (IOException ignored) {}
    }

    private static String routeKey(HttpMethod method, String path) {
        return method.name() + " " + path;
    }
}
