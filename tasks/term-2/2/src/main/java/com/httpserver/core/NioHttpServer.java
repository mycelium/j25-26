package com.httpserver.core;

import com.httpserver.api.HttpHandler;
import com.httpserver.api.HttpMethod;
import com.httpserver.api.HttpRequest;
import com.httpserver.api.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioHttpServer {

    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    
    // Хранилище маршрутов: Path -> (Method -> Handler)
    private final Map<String, Map<HttpMethod, HttpHandler>> routes = new HashMap<>();
    
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean isRunning = false;

    private NioHttpServer(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.threadCount = builder.threadCount;
        this.isVirtual = builder.isVirtual;
    }

    // публичный API

    public void addListener(HttpMethod method, String path, HttpHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
    }

    public void start() throws IOException {
        if (isVirtual) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executor = Executors.newFixedThreadPool(threadCount);
        }

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true); // Серверный сокет ждет подключений синхронно
        isRunning = true;

        System.out.println("HTTP Server started on " + host + ":" + port + " [Virtual Threads: " + isVirtual + "]");

        // Главный цикл приема соединений
        new Thread(() -> {
            while (isRunning) {
                try {
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(true); 
                    executor.submit(() -> handleClient(clientChannel));
                } catch (IOException e) {
                    if (isRunning) e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverChannel != null) serverChannel.close();
        if (executor != null) executor.shutdownNow();
    }

    private void handleClient(SocketChannel clientChannel) {
        try (clientChannel) {
            HttpRequest request = readAndParseRequest(clientChannel);
            if (request == null) return;

            HttpResponse response = processRequest(request);
            sendResponse(clientChannel, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpResponse processRequest(HttpRequest request) {
        Map<HttpMethod, HttpHandler> pathHandlers = routes.get(request.getPath());
        if (pathHandlers != null) {
            HttpHandler handler = pathHandlers.get(request.getMethod());
            if (handler != null) {
                try {
                    return handler.handle(request);
                } catch (Exception e) {
                    e.printStackTrace();
                    return HttpResponse.builder().statusCode(500).body("Internal Server Error").build();
                }
            }
        }
        return HttpResponse.builder().statusCode(404).body("Not Found").build();
    }

    private HttpRequest readAndParseRequest(SocketChannel channel) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int headerEndIndex = -1;

        // 1. Читаем до конца заголовков (\r\n\r\n)
        while (true) {
            buffer.clear();
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                if (headerBuffer.size() == 0) return null;
                break;
            }
            buffer.flip();
            byte[] chunk = new byte[buffer.remaining()];
            buffer.get(chunk);
            headerBuffer.write(chunk);

            byte[] currentData = headerBuffer.toByteArray();
            headerEndIndex = findByteArray(currentData, "\r\n\r\n".getBytes(StandardCharsets.US_ASCII), 0);
            if (headerEndIndex != -1) break;
        }

        if (headerEndIndex == -1) return null;

        byte[] allReadData = headerBuffer.toByteArray();
        String headerSection = new String(allReadData, 0, headerEndIndex, StandardCharsets.UTF_8);
        String[] headerLines = headerSection.split("\r\n");

        if (headerLines.length == 0) return null;

        // Request Line
        String[] reqLine = headerLines[0].split(" ");
        if (reqLine.length < 2) return null;
        HttpMethod method = HttpMethod.valueOf(reqLine[0].toUpperCase());
        String fullPath = reqLine[1];

        String path = fullPath;
        Map<String, String> queryParams = new HashMap<>();
        if (fullPath.contains("?")) {
            String[] parts = fullPath.split("\\?", 2);
            path = parts[0];
            String[] params = parts[1].split("&");
            for (String param : params) {
                String[] kv = param.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        // Headers
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            int colonIdx = headerLines[i].indexOf(':');
            if (colonIdx > 0) {
                headers.put(headerLines[i].substring(0, colonIdx).trim().toLowerCase(),
                        headerLines[i].substring(colonIdx + 1).trim());
            }
        }

        // Body
        int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0"));
        byte[] body = new byte[contentLength];

        int bodyBytesInFirstRead = allReadData.length - (headerEndIndex + 4);
        int bytesToCopy = Math.min(bodyBytesInFirstRead, contentLength);
        System.arraycopy(allReadData, headerEndIndex + 4, body, 0, bytesToCopy);

        int remainingBody = contentLength - bytesToCopy;
        if (remainingBody > 0) {
            readExact(channel, body, bytesToCopy, remainingBody);
        }

        // Парсинг Multipart
        List<HttpRequest.MultiPart> parts = new ArrayList<>();
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundaryStr = contentType.split("boundary=")[1];
            parts = parseMultipart(body, boundaryStr);
        }

        return new HttpRequest(method, path, queryParams, headers, body, parts);
    }

    private void readExact(SocketChannel channel, byte[] dest, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) break;
        }
        buffer.flip();
        buffer.get(dest, offset, buffer.remaining());
    }

    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        String statusLine = "HTTP/1.1 " + response.getStatusCode() + " OK\r\n";
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));

        Map<String, String> headers = response.getHeaders();
        headers.putIfAbsent("Content-Length", String.valueOf(response.getBody().length));
        headers.putIfAbsent("Connection", "close");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(response.getBody());

        ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    // multipart parset

    private List<HttpRequest.MultiPart> parseMultipart(byte[] body, String boundaryStr) {
        List<HttpRequest.MultiPart> parts = new ArrayList<>();
        byte[] boundary = ("--" + boundaryStr).getBytes(StandardCharsets.US_ASCII);
        byte[] endBoundary = ("--" + boundaryStr + "--").getBytes(StandardCharsets.US_ASCII);
        byte[] crlfCrlf = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

        int pos = 0;
        while (pos < body.length) {
            int partStart = findByteArray(body, boundary, pos);
            if (partStart == -1) break; // Больше нет boundary
            
            // Проверка на конечный boundary
            if (findByteArray(body, endBoundary, pos) == partStart) break;

            partStart += boundary.length + 2; // Пропускаем boundary и \r\n
            
            int headersEnd = findByteArray(body, crlfCrlf, partStart);
            if (headersEnd == -1) break;

            String headersStr = new String(body, partStart, headersEnd - partStart, StandardCharsets.UTF_8);
            Map<String, String> partHeaders = new HashMap<>();
            for (String hLine : headersStr.split("\r\n")) {
                int colonIdx = hLine.indexOf(':');
                if (colonIdx > 0) {
                    partHeaders.put(hLine.substring(0, colonIdx).trim().toLowerCase(),
                            hLine.substring(colonIdx + 1).trim());
                }
            }

            int contentStart = headersEnd + 4; // Пропускаем \r\n\r\n
            int nextBoundary = findByteArray(body, boundary, contentStart);
            if (nextBoundary == -1) break;

            int contentLen = nextBoundary - contentStart - 2; // -2 для убирания \r\n перед следующим boundary
            byte[] content = new byte[contentLen];
            System.arraycopy(body, contentStart, content, 0, contentLen);

            parts.add(new HttpRequest.MultiPart(partHeaders, content));
            pos = nextBoundary;
        }
        return parts;
    }

    private int findByteArray(byte[] source, byte[] match, int startIndex) {
        if (match.length == 0 || source.length == 0 || startIndex >= source.length) return -1;
        for (int i = startIndex; i <= source.length - match.length; i++) {
            boolean found = true;
            for (int j = 0; j < match.length; j++) {
                if (source[i + j] != match[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host = "127.0.0.1";
        private int port = 8080;
        private int threadCount = Runtime.getRuntime().availableProcessors();
        private boolean isVirtual = false;

        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder threadCount(int threadCount) { this.threadCount = threadCount; return this; }
        public Builder isVirtual(boolean isVirtual) { this.isVirtual = isVirtual; return this; }

        public NioHttpServer build() { return new NioHttpServer(this); }
    }
}