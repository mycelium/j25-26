package httpserver;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

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
    
    public void ignite() throws IOException {
        listener = ServerSocketChannel.open();
        listener.bind(new InetSocketAddress(host, port));
        active = true;
        
        System.out.printf("Server started on http://%s:%d%n", host, port);
        System.out.printf("Thread pool: %s%n", workers.getClass().getSimpleName());
        
        while (active) {
            try {
                SocketChannel socket = listener.accept();
                workers.submit(() -> dispatch(socket));
            } catch (ClosedByInterruptException e) {
                break;
            } catch (IOException e) {
                if (active) e.printStackTrace();
            }
        }
    }
    
    public void shutdown() throws IOException {
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
            System.err.println("Error handling request: " + e.getMessage());
        }
    }
    
    private Request parseIncoming(SocketChannel socket) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ByteBuffer chunk = ByteBuffer.allocate(4096);
        int lastMatch = -1;
        
        while (true) {
            chunk.clear();
            int read = socket.read(chunk);
            if (read == -1) return null;
            
            chunk.flip();
            byte[] data = new byte[chunk.remaining()];
            chunk.get(data);
            buffer.write(data);
            
            byte[] full = buffer.toByteArray();
            lastMatch = findSequence(full, "\r\n\r\n".getBytes());
            if (lastMatch != -1) break;
            
            if (full.length > 65536) return null;
        }
        
        byte[] raw = buffer.toByteArray();
        String headPart = new String(raw, 0, lastMatch, StandardCharsets.UTF_8);
        String[] lines = headPart.split("\r\n");
        
        if (lines.length == 0) return null;
        
        String[] rl = lines[0].split(" ");
        if (rl.length < 2) return null;
        String method = rl[0];
        String uri = rl[1];
        
        Map<String, String> headers = new LinkedHashMap<>();
        int idx = 1;
        while (idx < lines.length && !lines[idx].isEmpty()) {
            String line = lines[idx];
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim().toLowerCase();
                String val = line.substring(colon + 1).trim();
                headers.put(key, val);
            }
            idx++;
        }
        
        int bodyStart = lastMatch + 4;
        int contentLen = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        byte[] body = new byte[contentLen];
        
        int already = raw.length - bodyStart;
        int toCopy = Math.min(already, contentLen);
        System.arraycopy(raw, bodyStart, body, 0, toCopy);
        
        if (toCopy < contentLen) {
            int remain = contentLen - toCopy;
            ByteBuffer rest = ByteBuffer.allocate(remain);
            while (rest.hasRemaining()) {
                socket.read(rest);
            }
            rest.flip();
            rest.get(body, toCopy, remain);
        }
        
        List<FormPart> parts = Collections.emptyList();
        String ct = headers.get("content-type");
        if (ct != null && ct.startsWith("multipart/form-data")) {
            String boundary = extractBoundary(ct);
            parts = MultipartParser.parse(body, boundary);
        }
        
        return new Request(method, uri, headers, body, parts);
    }
    
    private int findSequence(byte[] haystack, byte[] needle) {
        outer: for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
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
                    return new Response().status(500).text("Internal error: " + e.getMessage());
                }
            }
        }
        
        Response resp = new Response();
        resp.status(404).text("Not found: " + req.getPath());
        return resp;
    }
    
    private void sendBack(SocketChannel socket, Response resp) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        String statusLine = String.format("HTTP/1.1 %d %s\r\n", resp.getStatus(), resp.getStatusText());
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
        
        for (Map.Entry<String, String> e : resp.getHeaders().entrySet()) {
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
        
        public Builder address(String host, int port) {
            this.host = host;
            this.port = port;
            return this;
        }
        
        public Builder workers(int count) {
            this.threads = count;
            return this;
        }
        
        public Builder virtual(boolean enabled) {
            this.useVirtual = enabled;
            return this;
        }
        
        public Server build() {
            return new Server(this);
        }
    }
}
