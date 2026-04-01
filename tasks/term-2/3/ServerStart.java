import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import json.MyJsonLibrary;
import http.HttpResponse;
import http.HttpServer;

public class ServerStart {
    private static final boolean USE_GSON = true; // true для использования Gson, false для MyJsonLibrary
    private static final boolean IS_VIRTUAL = true; // true для виртуальных потоков, false для фиксированного пула

    private static final String HOST = "localhost";
    private static final int PORT = 8082;
    private static final int THREAD_POOL_SIZE = 4;
    private static final Path FILE = Path.of("data.txt");
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        System.out.println("isVirtual = " + IS_VIRTUAL);
        System.out.println("useGson   = " + USE_GSON);
        HttpServer server = new HttpServer(HOST, PORT, IS_VIRTUAL, THREAD_POOL_SIZE);

        server.addRoutes("POST", "/request1", req -> {
            try {
                String body = req.getBody();
                Map<String, Object> data = parse(body);
                String json = toJson(data);
                Files.writeString(FILE, json + "\n",
                        StandardCharsets.UTF_8,
                        Files.exists(FILE)
                                ? java.nio.file.StandardOpenOption.APPEND
                                : java.nio.file.StandardOpenOption.CREATE);
                String content = Files.readString(FILE);
                return new HttpResponse(200, "application/json", content);
            } catch (Exception e) {
                return new HttpResponse(500, "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addRoutes("POST", "/request2", req -> {
            try {
                String body = req.getBody();
                Map<String, Object> data = parse(body);
                int number = ((Number) data.getOrDefault("number", 100)).intValue();
                int primes = countPrimes(number + 1000);
                Map<String, Object> result = new HashMap<>();
                result.put("input", number);
                result.put("primes", primes );
                return new HttpResponse(200, "application/json", toJson(result));

            } catch (Exception e) {
                return new HttpResponse(500, "application/json",
                        "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.start();
    }

    private static Map<String, Object> parse(String json) {
        if (USE_GSON) {
            return gson.fromJson(json, Map.class);
        } else {
            return MyJsonLibrary.parseToMap(json);
        }
    }

    private static String toJson(Object obj) {
        if (USE_GSON) {
            return gson.toJson(obj);
        } else {
            return MyJsonLibrary.toJson(obj);
        }
    }

    private static int countPrimes(int limit) {
        int count = 0;
        for (int i = 2; i < limit; i++) {
            if (isPrime(i)) count++;
        }
        return count;
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
