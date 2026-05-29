import org.example.http.HttpServer;
import org.example.http.HttpRequest;
import org.example.http.HttpResponse;
import Json;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerForTest {
    private static final ConcurrentHashMap<String, Long> memoryStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        boolean useVirtual = false;
        boolean useGson = false;

        // Парсинг аргументов командной строки
        for (String arg : args) {
            if ("--virtual".equals(arg)) useVirtual = true;
            if ("--gson".equals(arg)) useGson = true;
        }

        System.out.println("=== Server Configuration ===");
        System.out.println("Threads: " + (useVirtual ? "Virtual Threads" : "Classic Thread Pool"));
        System.out.println("JSON Library: " + (useGson ? "Gson" : "Own JsonLib"));
        System.out.println("============================");

        // Создаем сервер. 
        // ВАЖНО: Убедитесь, что класс HttpServer находится в classpath (ваш myhttp.jar)
        HttpServer server = new HttpServer("localhost", 8080, 10, useVirtual);

        // --- Endpoint 1: /store (I/O Bound) ---
        server.addRoute("/store", org.example.http.HttpMethod.POST, (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Map<String, Object> data;

                if (useGson) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    data = gson.fromJson(body, Map.class);
                } else {
                    // Используем ваш парсер из myjsonparser.jar
                    // Json.parse возвращает Object, приводим к Map
                    data = (Map<String, Object>) Json.parse(body);
                }

                String id = String.valueOf(data.getOrDefault("id", "0"));
                
                // I/O операция: запись в файл storage.txt
                Files.writeString(Path.of("storage.txt"), id + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                
                // Чтение всего файла (нагрузка на I/O)
                String storedContent = Files.readString(Path.of("storage.txt"));

                Map<String, String> response = new LinkedHashMap<>();
                response.put("status", "ok");
                response.put("id", id);
                response.put("file_length", String.valueOf(storedContent.length()));

                String jsonResp;
                if (useGson) {
                    jsonResp = new com.google.gson.Gson().toJson(response);
                } else {
                    // Используем ваш сериализатор
                    jsonResp = Json.toJson(response);
                }
                
                res.setBody(jsonResp);
                res.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                res.setStatus(500, "Internal Error");
                res.setBody("{\"error\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        });

        // --- Endpoint 2: /compute (CPU/Memory Bound) ---
        server.addRoute("/compute", org.example.http.HttpMethod.POST, (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Map<String, Object> data;

                if (useGson) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    data = gson.fromJson(body, Map.class);
                } else {
                    data = (Map<String, Object>) Json.parse(body);
                }

                int n = ((Number) data.getOrDefault("n", 10)).intValue();
                
                // CPU нагрузка: вычисление факториала
                long result = 1;
                for (int i = 2; i <= n; i++) result *= i;

                String key = "calc_" + System.currentTimeMillis();
                memoryStorage.put(key, result);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("result", result);
                response.put("key", key);
                response.put("map_size", memoryStorage.size());

                String jsonResp;
                if (useGson) {
                    jsonResp = new com.google.gson.Gson().toJson(response);
                } else {
                    jsonResp = Json.toJson(response);
                }

                res.setBody(jsonResp);
                res.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                res.setStatus(500, "Internal Error");
                res.setBody("{\"error\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        });

        System.out.println("Server started at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop.");
        server.start();
    }
}