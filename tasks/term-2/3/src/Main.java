import papkaJSON.*;
import papka_HTTP.*;
import com.google.gson.Gson;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String HOST = "localhost";
    private static final int PORT = 8083;
    private static final boolean VIRTUAL = true;
    private static final boolean GSON = false;
    private static final int THREAD_POOL_SIZE = 200;

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        Server_HTTP server = new Server_HTTP(PORT, HOST, THREAD_POOL_SIZE, VIRTUAL);

        server.addHandler("/request1", AllMethods.POST, (req, res) -> {
            try {
                String body = new String(req.getBody());
                Map<String, Object> data = parse(body);
                Files.writeString(Path.of("data.txt"), toJson(data));
                String stored = Files.readString(Path.of("data.txt"));
                Map<String, Object> storedData = parse(stored);

                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "ok");
                resp.put("stored", storedData);

                res.setStutus(200);
                res.setBody(toJson(resp));
            } catch (Exception e) {
                res.setStutus(500);
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addHandler("/request2", AllMethods.POST, (req, res) -> {
            try {
                String body = new String(req.getBody());
                Map<String, Object> data = parse(body);

                int sum = 0;
                Object nums = data.get("numbers");
                if (nums instanceof List) {
                    for (Object n : (List<?>) nums) {
                        if (n instanceof Number) sum += ((Number) n).intValue();
                    }
                }

                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "ok");
                resp.put("sum", sum);

                res.setStutus(200);
                res.setBody(toJson(resp));
            } catch (Exception e) {
                res.setStutus(500);
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        System.out.println("Port: " + PORT);
        server.startServer();
    }

    private static Map<String, Object> parse(String json) {
        if (GSON) {
            return gson.fromJson(json, Map.class);
        } else {
            return ConverterJSON.toMap(json);
        }
    }

    private static String toJson(Object obj) {
        if (GSON) {
            return gson.toJson(obj);
        } else {
            return ConverterJSON.toJSON(obj);
        }
    }
}