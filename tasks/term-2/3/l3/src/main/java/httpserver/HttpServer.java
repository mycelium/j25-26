package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HttpServer {
    private final String host;
    private final int port;
    private final int threads;
    private final boolean isVirtual;

    private final Map<String, Handler> routes = new ConcurrentHashMap<>();
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private HttpServer(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.isVirtual = isVirtual;
    }

    public static HttpServer create(String host, int port, int threads, boolean isVirtual) {
        return new HttpServer(host, port, threads, isVirtual);
    }

    public HttpServer addHandler(HttpMethod method, String path, Handler handler) {
        routes.put(method + " " + path, handler);
        return this;
    }

    public HttpServer get(String path, Handler h) { return addHandler(HttpMethod.GET, path, h); }
    public HttpServer post(String path, Handler h) { return addHandler(HttpMethod.POST, path, h); }
    public HttpServer put(String path, Handler h) { return addHandler(HttpMethod.PUT, path, h); }
    public HttpServer patch(String path, Handler h) { return addHandler(HttpMethod.PATCH, path, h); }
    public HttpServer delete(String path, Handler h) { return addHandler(HttpMethod.DELETE, path, h); }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already running");
        }

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threads);

        System.out.println("Server running on http://" + host + ":" + port);

        while (running.get()) {
            try {
                SocketChannel client = serverChannel.accept();
                if (client != null) {
                    executor.submit(() -> handle(client));
                }
            } catch (IOException e) {
                if (running.get()) System.err.println("Accept error: " + e.getMessage());
            }
        }
    }

    private void handle(SocketChannel channel) {
        try {
            channel.configureBlocking(true);
            channel.socket().setSoTimeout(3000);
            channel.socket().setSoLinger(true, 1);

            ByteBuffer inBuffer = ByteBuffer.allocate(8192);
            channel.read(inBuffer);
            inBuffer.flip();

            if (inBuffer.remaining() == 0) {
                channel.close();
                return;
            }

            byte[] requestBytes = new byte[inBuffer.remaining()];
            inBuffer.get(requestBytes);
            String requestText = new String(requestBytes, StandardCharsets.UTF_8);

            String[] firstLineParts = requestText.split("\r\n")[0].split(" ");
            if (firstLineParts.length < 2) {
                sendRaw(channel, 400, "Bad Request");
                return;
            }

            HttpMethod method = HttpMethod.from(firstLineParts[0]);
            String path = firstLineParts[1].split("\\?")[0];

            int contentLength = 0;
            for (String line : requestText.split("\r\n")) {
                String lower = line.toLowerCase();
                if (lower.startsWith("content-length:")) {
                    try {
                        contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                    } catch (Exception ignored) {}
                    break;
                }
            }

            int headerEnd = requestText.indexOf("\r\n\r\n");
            byte[] body = new byte[0];
            if (headerEnd >= 0 && contentLength > 0 && requestBytes.length > headerEnd + 4) {
                int bodyStart = headerEnd + 4;
                int bodyEnd = Math.min(bodyStart + contentLength, requestBytes.length);
                body = java.util.Arrays.copyOfRange(requestBytes, bodyStart, bodyEnd);
            }

            Map<String, String> headers = new ConcurrentHashMap<>();
            for (String line : requestText.split("\r\n")) {
                if (line.isEmpty() || !line.contains(":")) break;
                int c = line.indexOf(':');
                headers.put(line.substring(0, c).trim().toLowerCase(),
                        line.substring(c + 1).trim());
            }

            Request req = new Request(method, path, headers, body);
            Response res = new Response();

            String key = (method != null ? method : HttpMethod.GET) + " " + path;
            Handler handler = routes.get(key);

            if (handler != null) {
                handler.handle(req, res);
            } else if (hasPath(path)) {
                res.status(405).body("Method Not Allowed");
            } else {
                res.status(404).body("Not Found");
            }

            sendRaw(channel, res.status(), new String(res.toBytes(), StandardCharsets.UTF_8).split("\r\n\r\n", 2)[1]);

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            try { sendRaw(channel, 500, "Internal Error"); } catch (IOException ignored) {}
        } finally {
            try { channel.close(); } catch (IOException ignored) {}
        }
    }

    private void sendRaw(SocketChannel channel, int status, String bodyText) throws IOException {
        String reason = switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };

        byte[] bodyBytes = bodyText.getBytes(StandardCharsets.UTF_8);
        String response = "HTTP/1.1 " + status + " " + reason + "\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        ByteBuffer out = ByteBuffer.wrap(
                (response + bodyText).getBytes(StandardCharsets.UTF_8)
        );
        while (out.hasRemaining()) {
            channel.write(out);
        }
        channel.socket().shutdownOutput();
        channel.close();
    }

    private boolean hasPath(String path) {
        for (String key : routes.keySet()) {
            if (key.endsWith(" " + path)) return true;
        }
        return false;
    }

    public void stop() throws InterruptedException {
        running.set(false);
        try { serverChannel.close(); } catch (IOException ignored) {}
        if (executor != null) {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}