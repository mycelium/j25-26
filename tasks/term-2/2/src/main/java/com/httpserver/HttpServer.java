package com.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class HttpServer {
    private final int port;
    private final int threadCount;
    private final Map<String, Map<String, BiFunction<HttpRequest, HttpResponse, HttpResponse>>> routes;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean running;

    public HttpServer(int port, int threadCount) {
        this.port = port;
        this.threadCount = threadCount;
        this.routes = new HashMap<>();
        this.running = false;
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
            serverSocket = new ServerSocket(port);
            executor = Executors.newFixedThreadPool(threadCount);
            running = true;

            System.out.println("Server started on port " + port);

            while (running) {
                Socket client = serverSocket.accept();
                executor.execute(() -> handleClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket client) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter writer = new PrintWriter(client.getOutputStream())) {

            String line = reader.readLine();
            if (line == null) return;

            String[] parts = line.split(" ");
            String method = parts[0];
            String path = parts[1];

            System.out.println(method + " " + path);

            Map<String, String> headers = new HashMap<>();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] header = line.split(": ", 2);
                if (header.length == 2) {
                    headers.put(header[0], header[1]);
                }
            }

            StringBuilder body = new StringBuilder();
            if (headers.containsKey("Content-Length")) {
                int len = Integer.parseInt(headers.get("Content-Length"));
                char[] buffer = new char[len];
                reader.read(buffer, 0, len);
                body.append(buffer);
            }

            HttpRequest request = new HttpRequest(method, path, headers, body.toString());
            HttpResponse response = new HttpResponse();

            Map<String, BiFunction<HttpRequest, HttpResponse, HttpResponse>> methodRoutes = routes.get(method);
            if (methodRoutes != null && methodRoutes.containsKey(path)) {
                response = methodRoutes.get(path).apply(request, response);
            } else {
                response.status(404, "Not Found").body("404 - Not Found");
            }

            writer.print(response.toString());
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}