package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final String host;
    private final int port;
    private final ExecutorService executor;
    private final Map<String, Map<String, RequestHandler>> routes = new HashMap<>();
    private volatile boolean running = false;
    private ServerSocketChannel serverChannel;

    public HttpServer(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.executor = isVirtual
            ? Executors.newVirtualThreadPerTaskExecutor()
            : Executors.newFixedThreadPool(threads);
    }

    public void addHandler(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method.toUpperCase(), handler);
    }

    public void start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(host, port));
            running = true;
            System.out.println("Server started on " + host + ":" + port);

            while (running) {
                try {
                    SocketChannel clientChannel = serverChannel.accept();
                    executor.submit(() -> handleClient(clientChannel));
                } catch (IOException e) {
                    if (running) System.err.println("Accept error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
        executor.shutdown();
    }

    private void handleClient(SocketChannel clientChannel) {
        try (clientChannel) {
            byte[] requestBytes = readFully(clientChannel);
            if (requestBytes == null) return;

            String rawRequest = new String(requestBytes, StandardCharsets.UTF_8);
            HttpRequest request = parseRequest(rawRequest);
            if (request == null) return;

            HttpResponse response;
            Map<String, RequestHandler> pathHandlers = routes.get(request.getPath());

            if (pathHandlers == null) {
                response = new HttpResponse(404, "Not Found", "Route not found");
            } else if (!pathHandlers.containsKey(request.getMethod())) {
                response = new HttpResponse(405, "Method Not Allowed", "Method not allowed");
            } else {
                response = pathHandlers.get(request.getMethod()).handle(request);
            }

            sendResponse(clientChannel, response);
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    /** Reads the full request, growing the buffer as needed to handle bodies larger than 8 KB. */
    private byte[] readFully(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int bytesRead = channel.read(buffer);
        if (bytesRead <= 0) return null;

        // Check if there is more data (Content-Length header present and body incomplete)
        while (true) {
            String partial = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);
            int headerEnd = partial.indexOf("\r\n\r\n");
            if (headerEnd < 0) break; // headers not fully received yet — keep reading

            int contentLength = 0;
            for (String line : partial.substring(0, headerEnd).split("\r\n")) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                    break;
                }
            }

            int bodyReceived = buffer.position() - (headerEnd + 4);
            if (bodyReceived >= contentLength) break; // all data received

            // Need more data — expand buffer if necessary
            int totalExpected = headerEnd + 4 + contentLength;
            if (buffer.capacity() < totalExpected) {
                ByteBuffer bigger = ByteBuffer.allocate(totalExpected);
                buffer.flip();
                bigger.put(buffer);
                buffer = bigger;
            }

            int read = channel.read(buffer);
            if (read <= 0) break;
        }

        buffer.flip();
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    private HttpRequest parseRequest(String rawRequest) {
        String[] lines = rawRequest.split("\r\n");
        if (lines.length == 0) return null;

        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 2) return null;

        String method = requestLine[0];
        String fullPath = requestLine[1];

        // Split path and query string
        String path;
        Map<String, String> queryParams = new HashMap<>();
        int qIndex = fullPath.indexOf('?');
        if (qIndex >= 0) {
            path = fullPath.substring(0, qIndex);
            parseQueryString(fullPath.substring(qIndex + 1), queryParams);
        } else {
            path = fullPath;
        }

        Map<String, String> headers = new HashMap<>();
        int i = 1;
        while (i < lines.length && !lines[i].isEmpty()) {
            String[] parts = lines[i].split(": ", 2);
            if (parts.length == 2) headers.put(parts[0].toLowerCase(), parts[1]);
            i++;
        }

        String body = "";
        Map<String, String> formData = new HashMap<>();

        if (headers.containsKey("content-length")) {
            int bodyStart = rawRequest.indexOf("\r\n\r\n") + 4;
            if (bodyStart > 3 && bodyStart < rawRequest.length()) {
                body = rawRequest.substring(bodyStart);
            }

            String contentType = headers.get("content-type");
            if (contentType != null && contentType.contains("multipart/form-data")) {
                try {
                    String boundary = "--" + contentType.split("boundary=")[1];
                    for (String part : body.split(boundary)) {
                        if (part.isEmpty() || part.equals("--\r\n") || part.equals("--")) continue;
                        int headerEnd = part.indexOf("\r\n\r\n");
                        if (headerEnd < 0) continue;
                        String partHeaders = part.substring(0, headerEnd);
                        String partBody = part.substring(headerEnd + 4).trim();
                        int nameIndex = partHeaders.indexOf("name=\"");
                        if (nameIndex >= 0) {
                            int nameEnd = partHeaders.indexOf("\"", nameIndex + 6);
                            formData.put(partHeaders.substring(nameIndex + 6, nameEnd), partBody);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse multipart/form-data: " + e.getMessage());
                }
            }
        }

        return new HttpRequest(method, path, headers, body, formData, queryParams);
    }

    private void parseQueryString(String query, Map<String, String> out) {
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) out.put(decode(kv[0]), decode(kv[1]));
            else if (kv.length == 1 && !kv[0].isEmpty()) out.put(decode(kv[0]), "");
        }
    }

    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        var sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append("\r\n");

        for (var entry : response.getHeaders().entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        String body = response.getBody();
        if (body != null && !body.isEmpty()) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n\r\n");
            sb.append(body);
        } else {
            sb.append("Content-Length: 0\r\n\r\n");
        }

        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) channel.write(buffer);
    }
}
