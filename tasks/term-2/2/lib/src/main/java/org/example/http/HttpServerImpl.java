package org.example.http;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HttpServerImpl {

    private final String host;
    private final int port;
    private final int threads;
    private final boolean isVirtual;
    private final Map<String, Map<HttpMethod, HttpHandler>> routes;

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running = false;
    private Thread acceptThread;

    HttpServerImpl(String host, int port, int threads, boolean isVirtual,
                   Map<String, Map<HttpMethod, HttpHandler>> routes) {
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.isVirtual = isVirtual;
        this.routes = routes;
    }

    void start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(true);

            executor = isVirtual
                    ? Executors.newVirtualThreadPerTaskExecutor()
                    : Executors.newFixedThreadPool(threads);

            running = true;
            acceptThread = Thread.ofPlatform().name("http-accept").start(this::acceptLoop);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server on " + host + ":" + port, e);
        }
    }

    void stop() {
        running = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException ignored) {}
        if (executor != null) executor.shutdown();
        if (acceptThread != null) acceptThread.interrupt();
    }

    private void acceptLoop() {
        while (running) {
            try {
                SocketChannel channel = serverChannel.accept();
                if (channel != null) {
                    executor.submit(() -> handleConnection(channel));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept error: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        try (channel) {
            channel.configureBlocking(true);
            String rawRequest = readRequest(channel);
            if (rawRequest == null || rawRequest.isEmpty()) return;

            HttpRequest request = parseRequest(rawRequest);
            HttpResponse response = dispatch(request);
            sendResponse(channel, response);
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private String readRequest(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(65536);
        StringBuilder sb = new StringBuilder();
        int contentLength = -1;
        boolean headersComplete = false;

        while (true) {
            int read = channel.read(buffer);
            if (read == -1) break;
            buffer.flip();
            sb.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();

            String current = sb.toString();
            if (!headersComplete) {
                int headerEnd = current.indexOf("\r\n\r\n");
                if (headerEnd != -1) {
                    headersComplete = true;
                    String headers = current.substring(0, headerEnd);
                    for (String line : headers.split("\r\n")) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                        }
                    }
                    if (contentLength <= 0) break;
                    int bodyRead = current.length() - headerEnd - 4;
                    if (bodyRead >= contentLength) break;
                }
            } else {
                int headerEnd = current.indexOf("\r\n\r\n");
                int bodyRead = current.length() - headerEnd - 4;
                if (bodyRead >= contentLength) break;
            }
        }
        return sb.toString();
    }

    private HttpRequest parseRequest(String raw) {
        String[] parts = raw.split("\r\n\r\n", 2);
        String headerSection = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        String[] lines = headerSection.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        HttpMethod method = HttpMethod.valueOf(requestLine[0].toUpperCase());
        String fullPath = requestLine.length > 1 ? requestLine[1] : "/";

        // Parse path and query params
        String path = fullPath;
        Map<String, String> queryParams = new LinkedHashMap<>();
        int qIdx = fullPath.indexOf('?');
        if (qIdx != -1) {
            path = fullPath.substring(0, qIdx);
            String queryString = fullPath.substring(qIdx + 1);
            for (String param : queryString.split("&")) {
                String[] kv = param.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        // Parse headers
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colonIdx = lines[i].indexOf(':');
            if (colonIdx != -1) {
                String name = lines[i].substring(0, colonIdx).trim().toLowerCase();
                String value = lines[i].substring(colonIdx + 1).trim();
                headers.put(name, value);
            }
        }

        // Trim body to content-length if present
        String contentLengthStr = headers.get("content-length");
        if (contentLengthStr != null) {
            try {
                int cl = Integer.parseInt(contentLengthStr.trim());
                if (body.length() > cl) body = body.substring(0, cl);
            } catch (NumberFormatException ignored) {}
        }

        return new HttpRequest(method, path, headers, queryParams, body);
    }

    private HttpResponse dispatch(HttpRequest request) {
        Map<HttpMethod, HttpHandler> methodMap = routes.get(request.getPath());
        if (methodMap == null) {
            return new HttpResponse().status(404, "Not Found").text("404 Not Found");
        }
        HttpHandler handler = methodMap.get(request.getMethod());
        if (handler == null) {
            return new HttpResponse().status(405, "Method Not Allowed").text("405 Method Not Allowed");
        }
        try {
            return handler.handle(request);
        } catch (Exception e) {
            return new HttpResponse().status(500, "Internal Server Error").text("500 Internal Server Error: " + e.getMessage());
        }
    }

    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append("\r\n");

        String bodyStr = response.getBody();
        byte[] bodyBytes = bodyStr.getBytes(StandardCharsets.UTF_8);

        sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        sb.append("Connection: close\r\n");

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        buf.put(headerBytes);
        buf.put(bodyBytes);
        buf.flip();
        while (buf.hasRemaining()) channel.write(buf);
    }
}
