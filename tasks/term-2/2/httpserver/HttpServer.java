package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HttpServer {

    private static final Logger LOG = Logger.getLogger(HttpServer.class.getName());

    private final String host;
    private final int port;
    private final int threadPoolSize;
    private final boolean isVirtual;

    private final Map<HttpMethod, Map<String, HttpHandler>> routes = new ConcurrentHashMap<>();

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running;

    public HttpServer(String host, int port, int threadPoolSize, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.isVirtual = isVirtual;
    }

    public HttpServer addHandler(String path, HttpMethod method, HttpHandler handler) {
        routes.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
              .put(normalizePath(path), handler);
        return this;
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        executor = isVirtual ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(threadPoolSize);

        running = true;
        LOG.info("HTTP server listening on " + host + ":" + port);

        try {
            while (running) {
                SocketChannel client = serverChannel.accept();
                if (client != null) {
                    executor.submit(() -> handleClient(client));
                }
            }
        } catch (IOException e) {
            if (running) LOG.log(Level.SEVERE, "Accept loop error", e);
        } finally {
            shutdownExecutor();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null && serverChannel.isOpen()) serverChannel.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error closing server channel", e);
        }
    }

    private void shutdownExecutor() {
        if (executor == null) return;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            client.configureBlocking(true);

            Request  request  = parseRequest(client);
            Response response = new Response();

            HttpHandler handler = findHandler(request.getMethod(), request.getPath());
            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Handler threw an exception", e);
                    response.setStatus(500).setBody("Internal Server Error");
                }
            } else {
                boolean pathKnown = routes.values().stream()
                        .anyMatch(m -> m.containsKey(normalizePath(request.getPath())));
                if (pathKnown) {
                    response.setStatus(405).setBody("Method Not Allowed");
                } else {
                    response.setStatus(404).setBody("Not Found");
                }
            }

            writeResponse(client, response);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error handling client", e);
        }
    }

    private Request parseRequest(SocketChannel channel) throws IOException {
        NioLineReader reader = new NioLineReader(channel);

        // request
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) throw new IOException("Empty request");

        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 2) throw new IOException("Invalid request line: " + requestLine);

        HttpMethod method = HttpMethod.parse(parts[0]);
        if (method == null) throw new IOException("Unknown method: " + parts[0]);

        // ssplit pat
        String fullUri = parts[1];
        int qIdx = fullUri.indexOf('?');
        String path = qIdx >= 0 ? fullUri.substring(0, qIdx) : fullUri;
        String rawQuery = qIdx >= 0 ? fullUri.substring(qIdx + 1) : null;

        // headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim().toLowerCase(),
                            line.substring(colon + 1).trim());
            }
        }

        // send 100 Continue if requested
        String expect = headers.get("expect");
        if ("100-continue".equalsIgnoreCase(expect)) {
            writeFully(channel, ByteBuffer.wrap(
                    "HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.US_ASCII)));
        }

        // body
        byte[] body = new byte[0];
        String contentLengthStr = headers.get("content-length");
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr.trim());
            if (contentLength > 0) {
                body = reader.readBytes(contentLength);
            }
        }

        // multipart
        List<MultipartPart> parts2 = new ArrayList<>();
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                parts2 = MultipartParser.parse(body, boundary);
            }
        }

        return new Request(method, normalizePath(path), headers,
                           parseQueryString(rawQuery), body, parts2);
    }

    private void writeResponse(SocketChannel channel, Response response) throws IOException {
        byte[] body   = response.getBody();
        int    status = response.getStatus();

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(' ').append(reasonPhrase(status)).append("\r\n");
        sb.append("Server: httpserver-lib/1.0\r\n");

        if (response.getHeader("Content-Length") == null) {
            sb.append("Content-Length: ").append(body.length).append("\r\n");
        }

        for (Map.Entry<String, String> h : response.getHeaders().entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        writeFully(channel, ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
        if (body.length > 0) writeFully(channel, ByteBuffer.wrap(body));
    }

    private HttpHandler findHandler(HttpMethod method, String path) {
        Map<String, HttpHandler> byMethod = routes.get(method);
        return byMethod != null ? byMethod.get(path) : null;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        return path.startsWith("/") ? path : "/" + path;
    }

    private static void writeFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) channel.write(buffer);
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    private static String extractBoundary(String contentType) {
        for (String token : contentType.split(";")) {
            token = token.trim();
            if (token.startsWith("boundary=")) {
                String b = token.substring("boundary=".length()).trim();
                if (b.startsWith("\"") && b.endsWith("\"")) b = b.substring(1, b.length() - 1);
                return b;
            }
        }
        return null;
    }

    private static String reasonPhrase(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 503 -> "Service Unavailable";
            default  -> "Unknown";
        };
    }

    private static final class NioLineReader {

        private static final int CAPACITY = 16384;
        private static final int MASK     = CAPACITY - 1;

        private final SocketChannel channel;
        private final ByteBuffer readBuf = ByteBuffer.allocate(CAPACITY / 2);
        private final byte[] queue = new byte[CAPACITY];
        private int head, tail;

        NioLineReader(SocketChannel channel) {
            this.channel = channel;
        }

        String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean prevCR = false;
            while (true) {
                int b = nextByte();
                if (b == -1) return sb.isEmpty() ? null : sb.toString();
                if (prevCR && b == '\n') return sb.toString();   // strip \r
                if (b == '\r') { prevCR = true; continue; }
                if (b == '\n') return sb.toString();
                if (prevCR) { sb.append('\r'); prevCR = false; } // if CR
                sb.append((char) b);
            }
        }

        byte[] readBytes(int length) throws IOException {
            byte[] result = new byte[length];
            int written = 0;

            while (written < length && head != tail) {
                result[written++] = queue[head++ & MASK];
            }
            head &= MASK; tail &= MASK;

            ByteBuffer dst = ByteBuffer.wrap(result, written, length - written);
            while (dst.hasRemaining()) {
                int read = channel.read(dst);
                if (read == -1) throw new IOException("Connection closed while reading body");
            }
            return result;
        }

        private int nextByte() throws IOException {
            if (head == tail) refill();
            if (head == tail) return -1;
            return queue[head++ & MASK] & 0xFF;
        }

        private void refill() throws IOException {
            head = 0; tail = 0;
            readBuf.clear();
            int n = channel.read(readBuf);
            if (n <= 0) return;
            readBuf.flip();
            while (readBuf.hasRemaining()) {
                queue[tail++] = readBuf.get();
            }
        }
    }
}
