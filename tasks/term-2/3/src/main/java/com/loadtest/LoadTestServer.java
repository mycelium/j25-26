package com.loadtest;

import com.httpserver.core.NioHttpServer;
import com.httpserver.api.HttpMethod;
import com.httpserver.api.HttpResponse;
import org.example.json.Json; // Твой парсер из Лабы 1
import com.google.gson.Gson;   // Сторонняя библиотека

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class LoadTestServer {
    
    private static final boolean USE_VIRTUAL_THREADS = false;
    private static final boolean USE_GSON = true;

    private static final Gson gson = new Gson();
    private static final Path DB_FILE = Path.of("database.txt");

    // DTO для Request 1
    public static class DbRecord {
        public String id;
        public String data;
        public DbRecord() {}
    }

    // DTO для Request 2
    public static class CalcPayload {
        public int number;
        public CalcPayload() {}
    }

    public static class CalcResult {
        public int input;
        public long result;
        public CalcResult() {}
    }

    public static void main(String[] args) throws IOException {
        // Создаем "БД", если её нет
        if (!Files.exists(DB_FILE)) {
            Files.createFile(DB_FILE);
        }

        NioHttpServer server = NioHttpServer.builder()
                .host("127.0.0.1")
                .port(8080)
                .threadCount(200) // Важно для классических потоков при нагрузке
                .isVirtual(USE_VIRTUAL_THREADS)
                .build();

        // Request 1: I/O Нагрузка (Парсинг JSON -> Запись в файл -> Чтение)
        server.addListener(HttpMethod.POST, "/api/req1", request -> {
            try {
                String body = request.getBodyAsString();
                DbRecord record;
                
                // Переключатель парсеров
                if (USE_GSON) {
                    record = gson.fromJson(body, DbRecord.class);
                } else {
                    record = Json.parseObject(body, DbRecord.class);
                }

                // Пишем в файл (с синхронизацией, чтобы 500 потоков не сломали файл)
                String line = record.id + ":" + record.data + "\n";
                synchronized (LoadTestServer.class) {
                    Files.writeString(DB_FILE, line, StandardOpenOption.APPEND);
                }

                // Эмуляция retrieve: читаем весь файл в память
                String allData = Files.readString(DB_FILE);

                return HttpResponse.builder()
                        .statusCode(200)
                        .body("Saved. DB size in bytes: " + allData.length())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.builder().statusCode(500).body("Error").build();
            }
        });


        // Request 2: CPU Нагрузка (Парсинг JSON -> Сложные вычисления -> JSON)
        server.addListener(HttpMethod.POST, "/api/req2", request -> {
            try {
                String body = request.getBodyAsString();
                CalcPayload payload;
                
                if (USE_GSON) {
                    payload = gson.fromJson(body, CalcPayload.class);
                } else {
                    payload = Json.parseObject(body, CalcPayload.class);
                }

                // рекурсивный поиск числа Фибоначчи
                long calc = fibonacci(payload.number);

                CalcResult res = new CalcResult();
                res.input = payload.number;
                res.result = calc;

                // Сериализуем обратно
                String responseJson;
                if (USE_GSON) {
                    responseJson = gson.toJson(res);
                } else {
                    responseJson = Json.toJson(res);
                }

                return HttpResponse.builder()
                        .statusCode(200)
                        .header("Content-Type", "application/json")
                        .body(responseJson)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.builder().statusCode(500).body("Error").build();
            }
        });

        System.out.println("Starting Load Test Server...");
        System.out.println("-> Virtual Threads: " + USE_VIRTUAL_THREADS);
        System.out.println("-> JSON Parser: " + (USE_GSON ? "GSON" : "OWN (Lab 1)"));
        server.start();
    }

    // алгоритм для нагрузки CPU
    private static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}