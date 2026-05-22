import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerForTest {
    private static final ConcurrentHashMap<String, Long> memoryStorage = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        boolean useVirtual = false;
        boolean useGson = false;
        for (String arg : args) {
            if (arg.equals("--virtual")) useVirtual = true;
            if (arg.equals("--gson"))   useGson = true;
        }


        final boolean virtualFlag = useVirtual;
        final boolean gsonFlag = useGson;

        System.out.println("Threads: " + (virtualFlag ? "virtual" : "classic"));
        System.out.println("JSON library: " + (gsonFlag ? "Gson" : "own parser"));

        SimpleHttpServer server = new SimpleHttpServer("localhost", 8080, 10, virtualFlag);


        server.route("POST", "/store", (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Map<String, Object> data;
                if (gsonFlag) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    data = gson.fromJson(body, Map.class);
                } else {
                    data = SimpleJson.fromJson(body);
                }

                String id = String.valueOf(data.getOrDefault("id", "0"));
                Files.writeString(Path.of("storage.txt"), id + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                String stored = Files.readString(Path.of("storage.txt"));

                Map<String, String> response = new LinkedHashMap<>();
                response.put("status", "ok");
                response.put("id", id);
                response.put("stored", stored);

                String jsonResp = gsonFlag ? new com.google.gson.Gson().toJson(response) : SimpleJson.toJson(response);
                res.setBody(jsonResp);
                res.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                res.setStatus(500, "Internal Error");
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });


        server.route("POST", "/compute", (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Map<String, Object> data;
                if (gsonFlag) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    data = gson.fromJson(body, Map.class);
                } else {
                    data = SimpleJson.fromJson(body);
                }

                int n = ((Number) data.getOrDefault("n", 10)).intValue();
                long result = 1;
                for (int i = 2; i <= n; i++) result *= i;

                String key = "result_" + System.currentTimeMillis();
                memoryStorage.put(key, result);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("result", result);
                response.put("key", key);
                response.put("storage_size", memoryStorage.size());

                String jsonResp = gsonFlag ? new com.google.gson.Gson().toJson(response) : SimpleJson.toJson(response);
                res.setBody(jsonResp);
                res.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                res.setStatus(500, "Internal Error");
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        System.out.println("Server started at http://localhost:8080");
        server.start();
    }
}
