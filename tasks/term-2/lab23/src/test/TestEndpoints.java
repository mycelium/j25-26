package test;

import lab2.HttpServer;
import lab2.Request;
import lab2.Response;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestEndpoints {

    private static final String DATA_FILE = "test_data.json";
    private static final Map<String, String> MEMORY_STORE = new HashMap<>();
    

    private static GsonAdapter adapter;

    public static void setupServer(HttpServer server, boolean useGson) {
        adapter = new GsonAdapter(useGson);
        

        MEMORY_STORE.clear();
        try { if (new File(DATA_FILE).exists()) new File(DATA_FILE).delete(); } catch (Exception e) {}

        server.post("/api/file", (req, res) -> {
            try {
                String jsonBody = req.getBody();

                Object data = adapter.fromJson(jsonBody, Object.class);
                

                try (FileWriter fw = new FileWriter(DATA_FILE)) {
                    fw.write(adapter.toJson(data));
                }


                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                
                res.setStatus(200);
                res.setHeader("Content-Type", "application/json");
                res.setBody(sb.toString());
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("Error: " + e.getMessage());
            }
        });

        server.post("/api/memory", (req, res) -> {
            try {
                String jsonBody = req.getBody();
                String id = UUID.randomUUID().toString();

                Object data = adapter.fromJson(jsonBody, Object.class);
                MEMORY_STORE.put(id, adapter.toJson(data));

                String storedJson = MEMORY_STORE.get(id);
                
                res.setStatus(200);
                res.setHeader("Content-Type", "application/json");
                res.setBody(storedJson != null ? storedJson : "null");
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("Error: " + e.getMessage());
            }
        });
    }
}