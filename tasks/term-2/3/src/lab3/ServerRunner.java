package lab3;

import core.http.HttpServ;
import ru.lab.json.Json;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServerRunner {
    private static final String STORAGE_FILE = "data/storage.json";
    private static final Object FILE_LOCK = new Object();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        boolean isVirtual = false;
        boolean useOwnParser = false;

        for (String arg : args) {
            if (arg.equals("--virtual")) isVirtual = true;
            if (arg.equals("--own-parser")) useOwnParser = true;
        }

        HttpServ server = new HttpServ("localhost", 8080, 100, isVirtual);
        final boolean finalUseOwnParser = useOwnParser;

        // Эндпоинт /calculate (CPU-bound)
        server.addListener("POST", "/calculate", (req, res) -> {
            try {
                String body = req.getBody();
                
                if (body == null || body.trim().isEmpty()) {
                    res.setStatus(400);
                    res.setBody("{\"error\": \"Empty\"}");
                    return;
                }

                Map<String, Object> data;
                try {
                    data = finalUseOwnParser ? (Map<String, Object>) Json.fromJson(body) : gson.fromJson(body, Map.class);
                } catch (Exception e) {
                    res.setStatus(400);
                    res.setBody("{\"error\": \"Parse Error\"}");
                    return;
                }

                double sum = 0;
                for (Object val : data.values()) {
                    if (val instanceof Number) sum += ((Number) val).doubleValue();
                }

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("sum", sum);
                result.put("fields", data.size());

                String response = finalUseOwnParser ? Json.toJson(result) : gson.toJson(result);
                
                res.setStatus(200);
                res.addHeader("Content-Type", "application/json");
                res.setBody(response);
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("{\"error\": \"Internal Error\"}");
            }
        });
        
        // Эндпоинт /store (I/O-bound)
        server.addListener("POST", "/store", (req, res) -> {
            String body = req.getBody();
            
            try {
                Files.createDirectories(Paths.get("data"));
                // Используем PrintWriter для стабильной записи каждой строки
                synchronized (FILE_LOCK) {
                    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(STORAGE_FILE, true)))) {
                        out.println(body);
                    }
                }
            } catch (IOException e) {
                res.setStatus(500);
                res.setBody("{\"error\": \"Save Error\"}");
                return;
            }

            res.setStatus(200);
            res.addHeader("Content-Type", "application/json");
            res.setBody("{\"status\": \"ok\"}");
        });

        server.start();
    }
}