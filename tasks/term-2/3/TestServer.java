import server.HttpServer;
import server.HttpMethod;
import json.Json;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;

public class TestServer {

    static boolean USE_VIRTUAL = false;
    static boolean USE_GSON = false;

    public static void main(String[] args) {
        if (args.length >= 2) {
            USE_VIRTUAL = args[0].equals("virtual");
            USE_GSON = args[1].equals("gson");
        }

        HttpServer server = new HttpServer(8080, 10, USE_VIRTUAL);

        server.addRoute(HttpMethod.POST, "/io", (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Map<String, Object> data;
                if (USE_GSON) {
                    data = new Gson().fromJson(body, Map.class);
                } else {
                    data = Json.parseToMap(body);
                }

                File f = new File("test_output.txt");
                synchronized (TestServer.class) {
                    try (FileWriter fw = new FileWriter(f)) {
                        fw.write(data.toString());
                    }
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }

                String result;
                if (USE_GSON) {
                    result = new Gson().toJson(Map.of("chars", sb.length()));
                } else {
                    result = Json.toJson(Map.of("chars", sb.length()));
                }
                res.setHeader("Content-Type", "application/json");
                res.setBody(result);
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addRoute(HttpMethod.POST, "/compute", (req, res) -> {
            String body = req.getBodyAsString();
            Map<String, Object> data;
            if (USE_GSON) {
                data = new Gson().fromJson(body, Map.class);
            } else {
                data = Json.parseToMap(body);
            }

            int n = ((Number) data.get("n")).intValue();
            long sum = 0;
            for (int i = 1; i <= n; i++) {
                sum += (long) i * i;
            }

            String result;
            if (USE_GSON) {
                result = new Gson().toJson(Map.of("sum", sum));
            } else {
                result = Json.toJson(Map.of("sum", sum));
            }
            res.setHeader("Content-Type", "application/json");
            res.setBody(result);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("TestServer started (virtual=" + USE_VIRTUAL + ", gson=" + USE_GSON + ")");
        server.start();
    }
}
