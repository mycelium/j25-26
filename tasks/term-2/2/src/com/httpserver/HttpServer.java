package com.httpserver;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class HttpServer {
    private final String host;
    private final int port;
    private final Map<String, Map<String, RequestHandler>> routes = new HashMap<>();
    private ServerSocketChannel serverChannel;
    private volatile boolean running;
    private ExecutorService executor;

    public HttpServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static HttpServer create(String host, int port) {
        return new HttpServer(host, port);
    }

    public HttpServer get(String path, RequestHandler handler) {
        addRoute("GET", path, handler);
        return this;
    }

    public HttpServer post(String path, RequestHandler handler) {
        addRoute("POST", path, handler);
        return this;
    }

    public HttpServer put(String path, RequestHandler handler) {
        addRoute("PUT", path, handler);
        return this;
    }

    public HttpServer patch(String path, RequestHandler handler) {
        addRoute("PATCH", path, handler);
        return this;
    }

    public HttpServer delete(String path, RequestHandler handler) {
        addRoute("DELETE", path, handler);
        return this;
    }

    private void addRoute(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(method, m -> new HashMap<>()).put(path, handler);
    }

    public void start(int threadCount, boolean useVirtualThreads) throws IOException {
        if (useVirtualThreads && isVirtualThreadsSupported()) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
            System.out.println(" Using virtual threads");
        } else {
            executor = Executors.newFixedThreadPool(threadCount);
            System.out.println(" Using fixed thread pool with " + threadCount + " threads");
        }
        
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);
        running = true;

        System.out.println(" Server started on http://" + host + ":" + port);

        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    executor.submit(() -> handleClient(clientChannel));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ Accept error: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(SocketChannel clientChannel) {
        try {
            clientChannel.configureBlocking(true);
            InputStream in = clientChannel.socket().getInputStream();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
                String data = baos.toString();
                if (data.contains("\r\n\r\n")) {
                    int contentLength = extractContentLength(data);
                    if (contentLength > 0) {
                        int bodyStart = data.indexOf("\r\n\r\n") + 4;
                        int bodyReceived = baos.size() - bodyStart;
                        if (bodyReceived >= contentLength) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            
            HttpRequest request = HttpParser.parse(baos.toByteArray());
            HttpResponse response = routeRequest(request);
            
            clientChannel.write(java.nio.ByteBuffer.wrap(response.toBytes()));
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(clientChannel, 500, "Internal Server Error");
        } finally {
            try { clientChannel.close(); } catch (IOException e) {}
        }
    }

    private int extractContentLength(String data) {
        Pattern pattern = Pattern.compile("Content-Length:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private HttpResponse routeRequest(HttpRequest request) {
        Map<String, RequestHandler> methodRoutes = routes.get(request.method);
        if (methodRoutes == null) {
            return HttpResponse.notFound("Method not allowed");
        }
        
        RequestHandler handler = methodRoutes.get(request.path);
        if (handler == null) {
            return HttpResponse.notFound("Route not found: " + request.path);
        }
        
        return handler.handle(request);
    }

    private void sendErrorResponse(SocketChannel channel, int code, String message) {
        try {
            HttpResponse response = new HttpResponse(code, message);
            channel.write(java.nio.ByteBuffer.wrap(response.toBytes()));
        } catch (IOException ignored) {}
    }
    
    private boolean isVirtualThreadsSupported() {
        try {
            Class.forName("java.lang.Thread.Builder$VirtualThreadFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverChannel != null) serverChannel.close();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        System.out.println("Server stopped");
    }
}