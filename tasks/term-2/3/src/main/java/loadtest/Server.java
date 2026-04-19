package loadtest;

import com.google.gson.Gson;
import jsonparser.Json;
import server.HttpRequest;
import server.HttpResponse;
import server.HttpServer;
import server.HttpServerBuilder;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private static DatabaseHelper db;
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        Config config = Config.parse(args);

        db = new DatabaseHelper("data.db");
        db.init();

        HttpServer server = new HttpServerBuilder()
                .host("0.0.0.0")
                .port(config.port)
                .threadCount(config.threadCount)
                .useVirtualThreads(config.virtualThreads)
                .build();

        boolean useGson = config.useGson;

        // Request 1: parse JSON → store in SQLite → retrieve → return JSON
        server.addListener("/api/request1", HttpServer.HttpMethod.POST,
                (req, resp) -> handleRequest1(req, resp, useGson));

        // Request 2: parse JSON → compute Fibonacci → return JSON
        server.addListener("/api/request2", HttpServer.HttpMethod.POST,
                (req, resp) -> handleRequest2(req, resp, useGson));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { server.stop(); } catch (Exception ignored) {}
        }));

        server.start();
        System.out.printf("Listening on 0.0.0.0:%d  virtual=%s  gson=%s  threads=%d%n",
                config.port, config.virtualThreads, config.useGson, config.threadCount);

        // start() is non-blocking — park main thread
        Thread.currentThread().join();
    }

    private static void handleRequest1(HttpRequest req, HttpResponse resp, boolean useGson) {
        try {
            Map<String, Object> data = parseJson(req.getBodyAsString(), useGson);
            String name  = String.valueOf(data.getOrDefault("name", "unknown"));
            String value = String.valueOf(data.getOrDefault("value", ""));

            long id = db.insert(name, value);
            String retrieved = db.getById(id);

            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("retrieved", retrieved);
            result.put("status", "ok");

            resp.addHeader("Content-Type", "application/json")
                .setBody(toJson(result, useGson));
        } catch (Exception e) {
            resp.setStatus(500, "Internal Server Error")
                .setBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private static void handleRequest2(HttpRequest req, HttpResponse resp, boolean useGson) {
        try {
            Map<String, Object> data = parseJson(req.getBodyAsString(), useGson);
            int n = ((Number) data.getOrDefault("n", 10)).intValue();
            long result = fibonacci(n);

            Map<String, Object> response = new HashMap<>();
            response.put("n", n);
            response.put("result", result);
            response.put("status", "ok");

            resp.addHeader("Content-Type", "application/json")
                .setBody(toJson(response, useGson));
        } catch (Exception e) {
            resp.setStatus(500, "Internal Server Error")
                .setBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json, boolean useGson) {
        if (useGson) return gson.fromJson(json, Map.class);
        return Json.parseToMap(json);
    }

    private static String toJson(Map<String, Object> map, boolean useGson) {
        if (useGson) return gson.toJson(map);
        return Json.stringify(map);
    }

    private static long fibonacci(int n) {
        if (n <= 1) return Math.max(n, 0);
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long c = a + b; a = b; b = c;
        }
        return b;
    }
}
