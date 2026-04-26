package httpserver;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    
    private final String host;
    private final int port;
    private final ExecutorService workers;
    private final Map<String, Map<String, RequestHandler>> routes;
    private ServerSocketChannel listener;
    private volatile boolean active;
    
    private Server(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.routes = new ConcurrentHashMap<>();
        
        if (builder.useVirtual) {
            this.workers = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            this.workers = Executors.newFixedThreadPool(builder.threads);
        }
    }
    
    public static Builder configure() {
        return new Builder();
    }
    
    public Server on(String method, String path, RequestHandler handler) {
        String key = path.toLowerCase();
        routes.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
              .put(method.toUpperCase(), handler);
        return this;
    }
    
    public void ignite() throws java.io.IOException {
        listener = ServerSocketChannel.open();
        listener.bind(new InetSocketAddress(host, port));
        active = true;
        System.out.printf("Server started on http://%s:%d%n", host, port);
        
        while (active) {
            try {
                SocketChannel socket = listener.accept();
                workers.submit(() -> dispatch(socket));
            } catch (java.nio.channels.ClosedByInterruptException e) {
                break;
            } catch (java.io.IOException e) {
                if (active) e.printStackTrace();
            }
        }
    }
    
    public void shutdown() throws java.io.IOException {
        active = false;
        workers.shutdown();
        if (listener != null) listener.close();
    }
    
    private void dispatch(SocketChannel socket) {
        try (socket) {
            Request req = parseIncoming(socket);
            if (req == null) return;
            Response resp = routeRequest(req);
            sendBack(socket, resp);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private Request parseIncoming(SocketChannel socket) throws java.io.IOException {
        ByteArrayOutputStream allData = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int headerEnd = -1;
        byte[] allBytes = null;
        
        while (true) {
            buffer.clear();
            int read = socket.read(buffer);
            if (read == -1) return null;
            buffer.flip();
            byte[] chunk = new byte[buffer.remaining()];
            buffer.get(chunk);
            allData.write(chunk);
            allBytes = allData.toByteArray();
            headerEnd = findHeaderEnd(allBytes);
            if (headerEnd != -1) break;
            if (allBytes.length > 65536) return null;
        }
        
        String headerPart = new String(allBytes, 0, headerEnd, StandardCharsets.UTF_8);
        String[] lines = headerPart.split("\r\n");
        if (lines.length == 0) return null;
        
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 2) return null;
        String method = requestLine[0];
        String uri = requestLine[1];
        
        String path = uri;
        Map<String, String> queryParams = new LinkedHashMap<>();
        int qmIdx = uri.indexOf('?');
        if (qmIdx != -1) {
            path = uri.substring(0, qmIdx);
            String query = uri.substring(qmIdx + 1);
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                queryParams.put(decode(kv[0]), kv.length > 1 ? decode(kv[1]) : "");
            }
        }
        
        Map<String, String> headers = new LinkedHashMap<>();
        int contentLength = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim().toLowerCase();
                String val = line.substring(colon + 1).trim();
                headers.put(key, val);
                if (key.equals("content-length")) {
                    contentLength = Integer.parseInt(val);
                }
            }
        }
        
        byte[] body = new byte[contentLength];
        int alreadyRead = allBytes.length - (headerEnd + 4);
        int toCopy = Math.min(alreadyRead, contentLength);
        if (toCopy > 0) {
            System.arraycopy(allBytes, headerEnd + 4, body, 0, toCopy);
        }
        if (toCopy < contentLength) {
            int remaining = contentLength - toCopy;
            ByteBuffer bodyBuffer = ByteBuffer.allocate(remaining);
            while (bodyBuffer.hasRemaining()) {
                int bytesRead = socket.read(bodyBuffer);
                if (bytesRead == -1) break;
            }
            bodyBuffer.flip();
            bodyBuffer.get(body, toCopy, remaining);
        }
        
        java.util.List<FormPart> parts = java.util.Collections.emptyList();
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = extractBoundary(contentType);
            parts = MultipartParser.parse(body, boundary);
        }
        
        return new Request(method, uri, path, queryParams, headers, body, parts);
    }
    
    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i+1] == '\n' && data[i+2] == '\r' && data[i+3] == '\n') {
                return i;
            }
        }
        return -1;
    }
    
    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
    
    private String extractBoundary(String contentType) {
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                String b = part.substring("boundary=".length());
                if (b.startsWith("\"")) b = b.substring(1, b.length() - 1);
                return b;
            }
        }
        return "----";
    }
    
    private Response routeRequest(Request req) {
        String pathKey = req.getPath().toLowerCase();
        Map<String, RequestHandler> methodMap = routes.get(pathKey);
        if (methodMap != null) {
            RequestHandler handler = methodMap.get(req.getMethod());
            if (handler != null) {
                try {
                    return handler.handle(req);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response().status(500).text("Internal error");
                }
            }
        }
        return new Response().status(404).text("Not found");
    }
    
    private void sendBack(SocketChannel socket, Response resp) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String statusLine = String.format("HTTP/1.1 %d %s\r\n", resp.getStatus(), resp.getStatusText());
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = resp.getHeaders();
        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(resp.getBody().length));
        }
        headers.putIfAbsent("Connection", "close");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            out.write((e.getKey() + ": " + e.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(resp.getBody());
        ByteBuffer buf = ByteBuffer.wrap(out.toByteArray());
        while (buf.hasRemaining()) {
            socket.write(buf);
        }
    }
    
    public static class Builder {
        private String host = "localhost";
        private int port = 8080;
        private int threads = 4;
        private boolean useVirtual = false;
        
        public Builder address(String host, int port) { this.host = host; this.port = port; return this; }
        public Builder workers(int count) { this.threads = count; return this; }
        public Builder virtual(boolean enabled) { this.useVirtual = enabled; return this; }
        public Server build() { return new Server(this); }
    }
}
