package lab3;

import com.google.gson.Gson;
import httpserver.HttpServer;
import jsonlab.Json;

import java.util.List;
import java.util.Map;

/*
 * Аргументы запуска:
 *   [0] threadMode  — "virtual" | "classic"   (по умолчанию: virtual)
 *   [1] jsonMode    — "own"     | "gson"       (по умолчанию: own)
 *   [2] port        — порт сервера             (по умолчанию: 8080)
 *   [3] threads     — размер пула (только classic, по умолчанию: 8)
*/
 
public class Main {

    private static final String DATA_FILE = "stored_data.txt";

    public static void main(String[] args) throws Exception {
        String threadMode = args.length > 0 ? args[0] : "virtual";
        String jsonMode   = args.length > 1 ? args[1] : "own";
        int port          = args.length > 2 ? Integer.parseInt(args[2]) : 8080;
        int threads       = args.length > 3 ? Integer.parseInt(args[3]) : 8;

        boolean useVirtual = threadMode.equalsIgnoreCase("virtual");
        boolean useGson    = jsonMode.equalsIgnoreCase("gson");

        DataStorage storage = new DataStorage(DATA_FILE);
        Gson gson = new Gson();

        System.out.printf("=== Server ===%n");
        System.out.printf("  Threads : %s%s%n", threadMode, useVirtual ? "" : " (pool=" + threads + ")");
        System.out.printf("  JSON    : %s%n", jsonMode);
        System.out.printf("  Port    : %d%n%n", port);

        HttpServer server = HttpServer.create("localhost", port, threads, useVirtual)
            .post("/store", (req, res) -> {
                try {
                    String body = req.bodyAsString();
                    String value = parseValue(body, useGson, gson);

                    storage.write(value);
                    String stored = storage.read();

                    String responseJson = buildStoreResponse(stored, useGson, gson);
                    res.header("content-type", "application/json")
                       .body(responseJson);
                } catch (Exception e) {
                    res.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
                }
            })
            .post("/compute", (req, res) -> {
                try {
                    String body = req.bodyAsString();
                    long sum = computeSum(body, useGson, gson);

                    String responseJson = buildComputeResponse(sum, useGson, gson);
                    res.header("content-type", "application/json")
                       .body(responseJson);
                } catch (Exception e) {
                    res.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
                }
            });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                System.out.println("Server stopped.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        server.start();
    }
    @SuppressWarnings("unchecked")
    private static String parseValue(String body, boolean useGson, Gson gson) {
        if (useGson) {
            Map<?, ?> map = gson.fromJson(body, Map.class);
            return map.get("value").toString();
        } else {
            Map<String, Object> map = Json.decodeMap(body);
            return map.get("value").toString();
        }
    }

    private static String buildStoreResponse(String stored, boolean useGson, Gson gson) {
        if (useGson) {
            return gson.toJson(Map.of("stored", stored));
        } else {
            return Json.encode(Map.of("stored", stored));
        }
    }

    @SuppressWarnings("unchecked")
    private static long computeSum(String body, boolean useGson, Gson gson) {
        if (useGson) {
            Map<?, ?> map = gson.fromJson(body, Map.class);
            List<?> nums = (List<?>) map.get("numbers");
            return nums.stream().mapToLong(n -> ((Number) n).longValue()).sum();
        } else {
            Map<String, Object> map = Json.decodeMap(body);
            List<?> nums = (List<?>) map.get("numbers");
            return nums.stream().mapToLong(n -> ((Number) n).longValue()).sum();
        }
    }

    private static String buildComputeResponse(long sum, boolean useGson, Gson gson) {
        if (useGson) {
            return gson.toJson(Map.of("sum", sum));
        } else {
            return Json.encode(Map.of("sum", sum));
        }
    }
}
