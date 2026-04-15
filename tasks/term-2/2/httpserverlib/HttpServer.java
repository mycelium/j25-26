package httpserverlib;

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
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private final String host;
    private final int port;
    private final int threadPoolSize;
    private final boolean useVirtualThreads;
    private final Map<HttpMethod, Map<String, HttpHandler>> handlers = new ConcurrentHashMap<>();
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running;

    public HttpServer(String newHost, int newPort, int newThreadPoolSize, boolean newUseVirtualThreads) {
        this.host = newHost;
        this.port = newPort;
        this.threadPoolSize = newThreadPoolSize;
        this.useVirtualThreads = newUseVirtualThreads;
    }

    public HttpServer addListener(String path, HttpMethod method, HttpHandler handler) {
        Map<String, HttpHandler> methodHandlers = handlers.get(method);
        if (methodHandlers == null) {
            methodHandlers = new ConcurrentHashMap<>();
            handlers.put(method, methodHandlers);
        }
        methodHandlers.put(path, handler);
        return this;
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);
        running = true;

        if (useVirtualThreads) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executor = Executors.newFixedThreadPool(threadPoolSize);
        }

        System.out.println("Server started on " + host + ":" + port);

        while (running) {
            try {
                SocketChannel client = serverChannel.accept();
                executor.submit(() -> handleClient(client));
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverChannel != null && serverChannel.isOpen()) serverChannel.close();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleClient(SocketChannel client) {
        try {
            Request request = parseRequest(client);
            Response response = new Response();
            HttpHandler handler = findHandler(request.method, request.path);
            if (handler != null) {
                handler.handle(request, response);
            } else {
                response.setStatus(404);
                response.setBody("Not Found");
            }
            writeResponse(client, response);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            try { 
                client.close(); 
            } 
            catch (IOException ignored) {}
        }
    }

    private Request parseRequest(SocketChannel channel) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.socket().getInputStream()));
        String requestLine = reader.readLine();
        if (requestLine == null) throw new IOException("Empty request");
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) throw new IOException("Invalid request line");
        HttpMethod method = HttpMethod.valueOf(requestParts[0].toUpperCase());
        String fullPath = requestParts[1];
        int qIdx = fullPath.indexOf('?');
        String path = qIdx >= 0 ? fullPath.substring(0, qIdx) : fullPath;
        String query = qIdx >= 0 ? fullPath.substring(qIdx + 1) : null;

        Map<String, String> headers = new HashMap<>();
        String line;
        int contentLength = -1;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                headers.put(name, value);
                if (name.equals("content-length")) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }

        char[] bodyChars = new char[contentLength > 0 ? contentLength : 0];
        if (contentLength > 0) {
            int read = reader.read(bodyChars, 0, contentLength);
            if (read != contentLength) throw new IOException("Body read incomplete");
        }
        byte[] body = new String(bodyChars).getBytes();

        Map<String, String> params = parseQueryParams(query);
        Map<String, String> multipart = parseMultipartIfNeeded(headers, body);
        return new Request(method, path, headers, body, params, multipart);
    }

    private byte[] readBody(SocketChannel channel, int contentLength) throws IOException {
        if (contentLength <= 0) return new byte[0];
        ByteBuffer bodyBuf = ByteBuffer.allocate(contentLength);
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = channel.read(bodyBuf);
            if (read == -1) {
                throw new IOException("Connection closed while reading body");
            }
            totalRead += read;
        }
        return bodyBuf.array();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            params.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
        return params;
    }

    private Map<String, String> parseMultipartIfNeeded(Map<String, String> headers, byte[] body) {
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                return parseMultipart(body, boundary);
            }
        }
        return new HashMap<>();
    }

    private String extractBoundary(String contentType) {
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) return part.substring("boundary=".length());
        }
        return null;
    }

    private Map<String, String> parseMultipart(byte[] body, String boundary) {
        Map<String, String> fields = new HashMap<>();
        String bodyStr = new String(body);
        String delimiter = "--" + boundary;
        String[] parts = bodyStr.split(delimiter);
        for (String part : parts) {
            if (part.trim().isEmpty() || part.equals("--")) continue;
            int headerEnd = part.indexOf("\r\n\r\n");
            if (headerEnd < 0) continue;
            String headersPart = part.substring(0, headerEnd);
            String content = part.substring(headerEnd + 4);
            if (content.endsWith("\r\n")) content = content.substring(0, content.length() - 2);
            String name = null;
            for (String line : headersPart.split("\r\n")) {
                if (line.startsWith("Content-Disposition:")) {
                    int nameIdx = line.indexOf("name=\"");
                    if (nameIdx >= 0) {
                        int endIdx = line.indexOf("\"", nameIdx + 6);
                        if (endIdx > nameIdx) name = line.substring(nameIdx + 6, endIdx);
                    }
                    break;
                }
            }
            if (name != null) fields.put(name, content);
        }
        return fields;
    }

    private void writeFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private void writeResponse(SocketChannel channel, Response response) throws IOException {
        if (response.getBody().length > 0) {
            response.setHeader("Content-Length", String.valueOf(response.getBody().length));
        }

        String reason = getReasonPhrase(response.getStatus());
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatus()).append(" ").append(reason).append("\r\n");
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        writeFully(channel, ByteBuffer.wrap(headerBytes));

        if (response.getBody().length > 0) {
            writeFully(channel, ByteBuffer.wrap(response.getBody()));
        }
    }

    private String getReasonPhrase(int status) {
        switch (status) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }

    private HttpHandler findHandler(HttpMethod method, String path) {
        Map<String, HttpHandler> methodHandlers = handlers.get(method);
        return methodHandlers != null ? methodHandlers.get(path) : null;
    }
}