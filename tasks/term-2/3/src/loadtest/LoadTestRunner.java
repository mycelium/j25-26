package loadtest;

import myhttpserver.Handler;
import myhttpserver.Request;
import myhttpserver.Response;
import myhttpserver.Server;
import myjson.JsonParser;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LoadTestRunner {
    private static int PORT = 8085;
    private static int SERVER_THREADS_CLASSIC = 10;
    private static int WARMUP_REQUESTS = 1000;
    private static int WARMUP_CONCURRENCY = 50;
    private static int TEST_REQUESTS = 5000;
    private static int TEST_CONCURRENCY = 500;
    private static final int JVM_WARMUP_REQUESTS = 2000;
    private static final int JVM_WARMUP_CONCURRENCY = 20;

    private static final String REQUEST1_BODY = "{\"value\": \"test data from load test\"}";
    private static final String REQUEST2_BODY = "{\"n\": 20}";
    private static String REQUEST1_URL;   
    private static String REQUEST2_URL;

    record ResultRow(String configName, double avgReq1, int err1, double avgReq2, int err2) {}
    record Config(String name, boolean virtual, boolean useGson) {}

    public static void main(String[] args) throws Exception {
        parseArgs(args);
        REQUEST1_URL = "http://localhost:" + PORT + "/request1";
        REQUEST2_URL = "http://localhost:" + PORT + "/request2";

        Files.createDirectories(Path.of("storage"));

        Config[] configs = {
                new Config("Virtual + own parser", true, false),
                new Config("Virtual + Gson", true, true),
                new Config("Classic + own parser", false, false),
                new Config("Classic + Gson", false, true)
        };

        ResultRow[] results = new ResultRow[configs.length];

        for (int i = 0; i < configs.length; i++) {
            Config cfg = configs[i];
            printConfigBanner(cfg);

            Server server = new Server("localhost", PORT, SERVER_THREADS_CLASSIC, cfg.virtual);
            registerHandlers(server, cfg.useGson);

            Thread serverThread = new Thread(() -> {
                try { server.start(); } catch (IOException e) { e.printStackTrace(); }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            Thread.sleep(1000);

            System.out.println("JVM прогрев (JIT) ...");
            LoadTestClient.run(REQUEST1_URL, REQUEST1_BODY, JVM_WARMUP_REQUESTS, JVM_WARMUP_CONCURRENCY);
            LoadTestClient.run(REQUEST2_URL, REQUEST2_BODY, JVM_WARMUP_REQUESTS, JVM_WARMUP_CONCURRENCY);

            System.out.println("Прогрев (warm-up) ...");
            LoadTestClient.run(REQUEST1_URL, REQUEST1_BODY, WARMUP_REQUESTS, WARMUP_CONCURRENCY);
            LoadTestClient.run(REQUEST2_URL, REQUEST2_BODY, WARMUP_REQUESTS, WARMUP_CONCURRENCY);

            System.out.println("Основной тест ...");
            var res1 = LoadTestClient.run(REQUEST1_URL, REQUEST1_BODY, TEST_REQUESTS, TEST_CONCURRENCY);
            var res2 = LoadTestClient.run(REQUEST2_URL, REQUEST2_BODY, TEST_REQUESTS, TEST_CONCURRENCY);

            if (res1.errors() > 0) {
                System.out.printf(" ВНИМАНИЕ: Request-1 имел %d ошибок%n", res1.errors());
            }
            if (res2.errors() > 0) {
                System.out.printf(" ВНИМАНИЕ: Request-2 имел %d ошибок%n", res2.errors());
            }

            results[i] = new ResultRow(cfg.name, res1.avgMs(), res1.errors(),
                    res2.avgMs(), res2.errors());

            server.stop();
            Thread.sleep(1000);
        }

        printTable(results);
    }
    
    private static void printConfigBanner(Config cfg) {
        System.out.println("\n==========================================================");
        System.out.println("Запуск конфигурации: " + cfg.name());
        System.out.println("  Виртуальные потоки = " + cfg.virtual());
        System.out.println("  Использовать Gson   = " + cfg.useGson());
        System.out.println("==========================================================");
    }
    
    private static String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    private static void registerHandlers(Server server, boolean useGson) {
        Gson gson = useGson ? new Gson() : null;
        
        Handler request1Handler = (req, resp) -> {
        Path filePath = null;
        try {
            String body = new String(req.getBody(), StandardCharsets.UTF_8);
            Map<String, Object> data = parseJson(body, useGson, gson);
            String value = (String) data.get("value");
            if (value == null) {
               throw new IllegalArgumentException("Missing 'value' field in JSON");
            }
            String fileName = UUID.randomUUID() + ".tmp";
            filePath = Path.of("storage", fileName);
            Files.writeString(filePath, value, StandardCharsets.UTF_8);
           String retrieved = Files.readString(filePath, StandardCharsets.UTF_8);
           Map<String, Object> result = new HashMap<>();
           result.put("retrieved", retrieved);
           String jsonResp = toJson(result, useGson, gson);
        
           resp.setHeader("Content-Type", "application/json");
           resp.setBody(jsonResp);
           resp.setStatus(200);
        
        } catch (IllegalArgumentException e) {
           resp.setStatus(400);
           resp.setBody("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (IOException e) {
          resp.setStatus(500);
          resp.setBody("{\"error\":\"I/O error: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
           resp.setStatus(500);
           resp.setBody("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } finally {
            if (filePath != null) {
              try {
                  Files.deleteIfExists(filePath);
               } catch (IOException e) {
                  System.err.println("Failed to delete temp file: " + filePath);
                  e.printStackTrace();  
               }
             }
          }
       };

        

        Handler request2Handler = (req, resp) -> {
            try {
                String body = new String(req.getBody(), StandardCharsets.UTF_8);
                Map<String, Object> data = parseJson(body, useGson, gson);
                int n = ((Number) data.get("n")).intValue();
                long fib = fibonacci(n);

                Map<String, Object> result = new HashMap<>();
                result.put("n", n);
                result.put("result", fib);
                String jsonResp = toJson(result, useGson, gson);

                resp.setHeader("Content-Type", "application/json");
                resp.setBody(jsonResp);
                resp.setStatus(200);
            } catch (Exception e) {
                resp.setStatus(500);
                resp.setBody("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        };

        server.addHandler(Server.Method.POST, "/request1", request1Handler);
        server.addHandler(Server.Method.POST, "/request2", request2Handler);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json, boolean useGson, Gson gson) {
        if (useGson) return gson.fromJson(json, Map.class);
        else return JsonParser.parseToMap(json);
    }

    private static String toJson(Map<String, Object> map, boolean useGson, Gson gson) {
        if (useGson) return gson.toJson(map);
        else return JsonParser.toJson(map);
    }

    private static long fibonacci(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) { long c = a + b; a = b; b = c; }
        return b;
    }

    private static void printTable(ResultRow[] results) {
        System.out.println("\n\n============================================================");
        System.out.println("ИТОГОВАЯ ТАБЛИЦА (среднее время на запрос, мс)");
        System.out.println("Параметры теста: запросов=" + TEST_REQUESTS
                + ", параллелизм=" + TEST_CONCURRENCY);
        System.out.println("============================================================");
        System.out.printf("%-20s | %-10s | %-10s | %-10s | %-10s%n",
                "Конфигурация", "Req1 avg", "Req1 err", "Req2 avg", "Req2 err");
        System.out.println("-------------------------------------------------------------------");
        for (ResultRow row : results) {
            System.out.printf("%-20s | %10.2f | %10d | %10.2f | %10d%n",
                    row.configName, row.avgReq1, row.err1, row.avgReq2, row.err2);
        }
        System.out.println("============================================================");
    }

    private static void parseArgs(String[] args) {
        var argMap = new HashMap<String, String>();
        boolean helpRequested = false;
        for (int i = 0; i < args.length; i++) {
             if (args[i].equals("--help") || args[i].equals("-h")) {
                helpRequested = true;
            }
            if (args[i].startsWith("--") && i + 1 < args.length) {
                argMap.put(args[i], args[++i]);
            }
        }

        PORT = getInt(argMap, "--port", PORT);
        SERVER_THREADS_CLASSIC = getInt(argMap, "--server-threads", SERVER_THREADS_CLASSIC);
        WARMUP_REQUESTS = getInt(argMap, "--warmup-requests", WARMUP_REQUESTS);
        WARMUP_CONCURRENCY = getInt(argMap, "--warmup-concurrency", WARMUP_CONCURRENCY);
        TEST_REQUESTS = getInt(argMap, "--test-requests", TEST_REQUESTS);
        TEST_CONCURRENCY = getInt(argMap, "--test-concurrency", TEST_CONCURRENCY);

        if (helpRequested) {
            printHelp();
            System.exit(0);
        }
    }

    private static int getInt(Map<String, String> args, String key, int defaultValue) {
        String val = args.get(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    private static void printHelp() {
        var help = """
                Использование: java loadtest.LoadTestRunner [параметры]

                Параметры:
                  --port <число>              Порт сервера (по умолчанию: 8085)
                  --server-threads <число>    Размер классического пула потоков (по умолчанию: 10)
                  --warmup-requests <число>   Количество прогревочных запросов (по умолчанию: 1000)
                  --warmup-concurrency <число> Параллелизм прогрева (по умолчанию: 50)
                  --test-requests <число>     Количество замеряемых запросов (по умолчанию: 5000)
                  --test-concurrency <число>  Параллелизм основного теста (по умолчанию: 100)
                  --help, -h                  Показать справку

                Пример:
                  java loadtest.LoadTestRunner --port 8090 --test-requests 2000 --test-concurrency 80
                """;
        System.out.println(help);
    }
}