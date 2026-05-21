import com.google.gson.Gson;
import httpserver.HttpMethod;
import httpserver.HttpServer;
import jsonparser.JsonMapper;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PerformanceServer {

    static final boolean USE_GSON            = true;  // true = Gson, false = собственный парсер
    static final boolean USE_VIRTUAL_THREADS = false;   // true = virtual, false = classic
    static final int     THREAD_POOL_SIZE    = 16;     // размер пула (только для classic)
    static final int     PORT                = 8080;

    private static final Path   STORAGE_FILE = Paths.get("storage.txt");
    private static final Object FILE_LOCK    = new Object();

    public static void main(String[] args) throws IOException {
        if (!Files.exists(STORAGE_FILE)) {
            Files.createFile(STORAGE_FILE);
        }

        Gson gson = new Gson();

        HttpServer server = new HttpServer("0.0.0.0", PORT, THREAD_POOL_SIZE, USE_VIRTUAL_THREADS);

        server.addRoute(HttpMethod.POST, "/store", (req, res) -> {
            try {
                String body = req.getBodyAsString();

                String data;
                if (USE_GSON) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = gson.fromJson(body, Map.class);
                    data = String.valueOf(map.getOrDefault("data", ""));
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) JsonMapper.fromJson(body);
                    data = String.valueOf(map.getOrDefault("data", ""));
                }

                String line = data + System.lineSeparator();
                synchronized (FILE_LOCK) {
                    Files.writeString(STORAGE_FILE, line, StandardOpenOption.APPEND);
                }

                String retrieved;
                synchronized (FILE_LOCK) {
                    java.util.List<String> lines = Files.readAllLines(STORAGE_FILE);
                    retrieved = lines.isEmpty() ? "" : lines.get(lines.size() - 1);
                }

                String responseJson;
                if (USE_GSON) {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("stored", data);
                    resp.put("retrieved", retrieved);
                    responseJson = gson.toJson(resp);
                } else {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("stored", data);
                    resp.put("retrieved", retrieved);
                    responseJson = JsonMapper.toJson(resp);
                }

                res.setStatus(200)
                   .setHeader("Content-Type", "application/json")
                   .setBody(responseJson);

            } catch (Exception e) {
                res.setStatus(500).setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addRoute(HttpMethod.POST, "/calc", (req, res) -> {
            try {
                String body = req.getBodyAsString();

                double a, b;
                if (USE_GSON) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = gson.fromJson(body, Map.class);
                    a = ((Number) map.getOrDefault("a", 0.0)).doubleValue();
                    b = ((Number) map.getOrDefault("b", 0.0)).doubleValue();
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) JsonMapper.fromJson(body);
                    a = ((Number) map.getOrDefault("a", 0)).doubleValue();
                    b = ((Number) map.getOrDefault("b", 0)).doubleValue();
                }

                double result = a + b;

                String responseJson;
                if (USE_GSON) {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("result", result);
                    responseJson = gson.toJson(resp);
                } else {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("result", result);
                    responseJson = JsonMapper.toJson(resp);
                }

                res.setStatus(200)
                   .setHeader("Content-Type", "application/json")
                   .setBody(responseJson);

            } catch (Exception e) {
                res.setStatus(500).setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        System.out.printf(
            "Server started on port %d | virtual=%b | gson=%b | poolSize=%d%n",
            PORT, USE_VIRTUAL_THREADS, USE_GSON, THREAD_POOL_SIZE
        );

        server.start();
    }
}
