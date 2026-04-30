package com.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class HttpServer {
    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    private final Map<String, Map<String, BiFunction<HttpRequest, HttpResponse, HttpResponse>>> routes;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running;

    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;
        this.routes = new HashMap<>();
        this.running = false;
    }

    public HttpServer(ServerConfig config) {
        this(config.getHost(), config.getPort(), config.getThreadCount(), config.isUseVirtualThreads());
    }

    public HttpServer get(String path, BiFunction<HttpRequest, HttpResponse, HttpResponse> handler) {
        routes.computeIfAbsent("GET", k -> new HashMap<>()).put(path, handler);
        return this;
    }

    public HttpServer post(String path, BiFunction<HttpRequest, HttpResponse, HttpResponse> handler) {
        routes.computeIfAbsent("POST", k -> new HashMap<>()).put(path, handler);
        return this;
    }

    public HttpServer put(String path, BiFunction<HttpRequest, HttpResponse, HttpResponse> handler) {
        routes.computeIfAbsent("PUT", k -> new HashMap<>()).put(path, handler);
        return this;
    }

    public HttpServer patch(String path, BiFunction<HttpRequest, HttpResponse, HttpResponse> handler) {
        routes.computeIfAbsent("PATCH", k -> new HashMap<>()).put(path, handler);
        return this;
    }

    public HttpServer delete(String path, BiFunction<HttpRequest, HttpResponse, HttpResponse> handler) {
        routes.computeIfAbsent("DELETE", k -> new HashMap<>()).put(path, handler);
        return this;
    }

    public void start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(host, port));

            executor = isVirtual
                    ? Executors.newVirtualThreadPerTaskExecutor()
                    : Executors.newFixedThreadPool(threadCount);

            running = true;
            System.out.println("Server started on " + host + ":" + port
                    + " (virtual=" + isVirtual + ", threads=" + threadCount + ")");

            while (running) {
                try {
                    SocketChannel client = serverChannel.accept();
                    executor.execute(() -> handleClient(client));
                } catch (ClosedChannelException e) {
                    if (running) throw e;
                    break;
                }
            }
        } catch (IOException e) {
            throw new HttpServerException("Failed to start server", e);
        }
    }

    private void handleClient(SocketChannel client) {
        try (SocketChannel ch = client;
             InputStream in = Channels.newInputStream(ch);
             OutputStream out = Channels.newOutputStream(ch)) {

            String requestLine = readLine(in);
            if (requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            if (parts.length < 3) return;
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            String headerLine;
            while ((headerLine = readLine(in)) != null && !headerLine.isEmpty()) {
                int colon = headerLine.indexOf(':');
                if (colon > 0) {
                    String name = headerLine.substring(0, colon).trim();
                    String value = headerLine.substring(colon + 1).trim();
                    headers.put(name, value);
                }
            }

            byte[] body = new byte[0];
            String cl = headers.get("Content-Length");
            if (cl != null) {
                int len = Integer.parseInt(cl.trim());
                if (len > 0) body = in.readNBytes(len);
            }

            HttpRequest request = new HttpRequest(method, path, headers, body);
            HttpResponse response = new HttpResponse();

            Map<String, BiFunction<HttpRequest, HttpResponse, HttpResponse>> methodRoutes = routes.get(method);
            if (methodRoutes != null && methodRoutes.containsKey(path)) {
                response = methodRoutes.get(path).apply(request, response);
            } else {
                response.status(404, "Not Found").body("404 - Not Found");
            }

            out.write(response.toBytes());
            out.flush();
        } catch (IOException e) {
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int prev = -1, cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') {
                byte[] data = buf.toByteArray();
                return new String(data, 0, data.length - 1, StandardCharsets.ISO_8859_1);
            }
            buf.write(cur);
            prev = cur;
        }
        if (buf.size() == 0) return null;
        return buf.toString(StandardCharsets.ISO_8859_1);
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException ignored) {
        }
        if (executor != null) executor.shutdown();
    }
}