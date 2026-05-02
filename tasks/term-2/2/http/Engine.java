package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Engine {
    private final String address;
    private final int port;
    private final int workersCount;
    private final boolean useVirtual;
    
    // Плоская структура: ключ вида "GET /path"
    private final Map<String, RequestHandler> endpoints = new ConcurrentHashMap<>();
    
    private ServerSocketChannel serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isActive;

    public Engine(String address, int port, int workersCount, boolean useVirtual) {
        this.address = address;
        this.port = port;
        this.workersCount = workersCount;
        this.useVirtual = useVirtual;
    }

    public Engine route(ReqMethod method, String path, RequestHandler handler) {
        endpoints.put(method.name() + " " + path, handler);
        return this;
    }

    public void launch() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(address, port));
        serverSocket.configureBlocking(true);
        isActive = true;

        threadPool = useVirtual ? Executors.newVirtualThreadPerTaskExecutor() 
                                : Executors.newFixedThreadPool(workersCount);

        System.out.println("WebEngine is listening on " + address + ":" + port);

        while (isActive) {
            try {
                SocketChannel connection = serverSocket.accept();
                threadPool.submit(() -> processConnection(connection));
            } catch (IOException e) {
                if (isActive) e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        isActive = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (threadPool != null) threadPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processConnection(SocketChannel channel) {
        try {
            HttpRequest req = buildRequest(channel);
            HttpResponse res = new HttpResponse();
            
            RequestHandler handler = endpoints.get(req.getMethod().name() + " " + req.getRoute());
            
            if (handler != null) {
                handler.process(req, res);
            } else {
                res.setStatusCode(404);
                res.setPayload("404 - Endpoint not found");
            }
            
            sendResponse(channel, res);
            channel.close();
        } catch (Exception e) {
            System.err.println("Request error: " + e.getMessage());
            try { channel.close(); } catch (IOException ignored) {}
        }
    }

    private HttpRequest buildRequest(SocketChannel channel) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(channel.socket().getInputStream()));
        
        String startLine = br.readLine();
        if (startLine == null) throw new IOException("Empty connection");
        
        String[] parts = startLine.split(" ");
        ReqMethod method = ReqMethod.valueOf(parts[0].toUpperCase());
        
        String[] pathParts = parts[1].split("\\?", 2);
        String route = pathParts[0];
        String query = pathParts.length > 1 ? pathParts[1] : "";

        Map<String, String> headers = new HashMap<>();
        int bodySize = 0;
        String headerLine;
        
        while ((headerLine = br.readLine()) != null && !headerLine.isEmpty()) {
            String[] hParts = headerLine.split(":", 2);
            if (hParts.length == 2) {
                String key = hParts[0].trim().toLowerCase();
                String val = hParts[1].trim();
                headers.put(key, val);
                if (key.equals("content-length")) {
                    bodySize = Integer.parseInt(val);
                }
            }
        }

        char[] buffer = new char[bodySize];
        if (bodySize > 0) {
            br.read(buffer, 0, bodySize);
        }
        byte[] payload = new String(buffer).getBytes();

        Map<String, String> qParams = parseForm(query, "&");
        Map<String, String> formData = extractMultipart(headers, payload);

        return new HttpRequest(method, route, headers, payload, qParams, formData);
    }

    private Map<String, String> parseForm(String raw, String delimiter) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) return map;
        for (String pair : raw.split(delimiter)) {
            String[] kv = pair.split("=", 2);
            map.put(kv[0], kv.length == 2 ? kv[1] : "");
        }
        return map;
    }

    private Map<String, String> extractMultipart(Map<String, String> headers, byte[] payload) {
        String cType = headers.get("content-type");
        if (cType == null || !cType.contains("multipart/form-data")) return new HashMap<>();
        
        String boundary = null;
        for (String p : cType.split(";")) {
            if (p.trim().startsWith("boundary=")) {
                boundary = p.trim().substring(9);
            }
        }
        if (boundary == null) return new HashMap<>();

        Map<String, String> result = new HashMap<>();
        String payloadStr = new String(payload);
        String[] chunks = payloadStr.split("--" + boundary);
        
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty() || chunk.trim().equals("--")) continue;
            String[] sections = chunk.split("\r\n\r\n", 2);
            if (sections.length < 2) continue;
            
            String head = sections[0];
            String body = sections[1];
            if (body.endsWith("\r\n")) body = body.substring(0, body.length() - 2);

            if (head.contains("name=\"")) {
                int start = head.indexOf("name=\"") + 6;
                int end = head.indexOf("\"", start);
                if (end > start) {
                    result.put(head.substring(start, end), body);
                }
            }
        }
        return result;
    }

    private void sendResponse(SocketChannel channel, HttpResponse res) throws IOException {
        if (res.getPayload().length > 0) {
            res.addHeader("Content-Length", String.valueOf(res.getPayload().length));
        }

        String msg = switch (res.getStatusCode()) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            default -> "Error";
        };

        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(res.getStatusCode()).append(" ").append(msg).append("\r\n");
        res.getHeaders().forEach((k, v) -> builder.append(k).append(": ").append(v).append("\r\n"));
        builder.append("\r\n");

        ByteBuffer headBuf = ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
        while (headBuf.hasRemaining()) channel.write(headBuf);

        if (res.getPayload().length > 0) {
            ByteBuffer bodyBuf = ByteBuffer.wrap(res.getPayload());
            while (bodyBuf.hasRemaining()) channel.write(bodyBuf);
        }
    }
}