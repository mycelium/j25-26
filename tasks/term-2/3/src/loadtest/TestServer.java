package loadtest;

import httpserver.Server;
import httpserver.Request;
import httpserver.Response;
import java.nio.file.*;
import java.util.*;

public class TestServer {
    
    private static final String STORAGE_FILE = "test_data.txt";
    private static final Object FILE_LOCK = new Object();
    
    public static void main(String[] args) throws Exception {
        
        System.out.println("========================================");
        System.out.println("STARTING LOAD TEST SERVER");
        System.out.println("========================================");
        System.out.println("Virtual threads: " + Config.USE_VIRTUAL_THREADS);
        System.out.println("JSON parser: " + (Config.USE_GSON ? "Gson" : "Own"));
        System.out.println("Port: " + Config.PORT);
        System.out.println("========================================\n");
        
        Path storagePath = Path.of(STORAGE_FILE);
        if (Files.exists(storagePath)) {
            Files.delete(storagePath);
        }
        Files.createFile(storagePath);
        
        Server server = Server.configure()
                .address(Config.HOST, Config.PORT)
                .workers(Config.THREAD_POOL_SIZE)
                .virtual(Config.USE_VIRTUAL_THREADS)
                .build();
        
        server.on("POST", "/store", req -> {
            String body = req.getBodyText();
            Map<String, Object> data = parseJson(body);
            
            String id = String.valueOf(data.getOrDefault("id", "unknown"));
            String value = String.valueOf(data.getOrDefault("data", "empty"));
            String record = id + ":" + value + "\n";
            
            synchronized (FILE_LOCK) {
                Files.writeString(storagePath, record, StandardOpenOption.APPEND);
                Files.readString(storagePath);
            }
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "ok");
            result.put("message", "saved");
            
            String responseJson;
            if (Config.USE_GSON) {
                responseJson = new com.google.gson.Gson().toJson(result);
            } else {
                responseJson = jsonparser.JsonLib.stringify(result);
            }
            
            return new Response().status(200).header("Content-Type", "application/json").text(responseJson);
        });
        
        server.on("POST", "/calculate", req -> {
            String body = req.getBodyText();
            Map<String, Object> data = parseJson(body);
            
            int number = 20;
            if (data.containsKey("n")) {
                number = ((Number) data.get("n")).intValue();
            }
            
            long result = fibonacci(number);
            
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("input", number);
            resultMap.put("result", result);
            
            String responseJson;
            if (Config.USE_GSON) {
                responseJson = new com.google.gson.Gson().toJson(resultMap);
            } else {
                responseJson = jsonparser.JsonLib.stringify(resultMap);
            }
            
            return new Response().status(200).header("Content-Type", "application/json").text(responseJson);
        });
        
        System.out.println("Server started on http://" + Config.HOST + ":" + Config.PORT);
        server.ignite();
    }
    
    private static Map<String, Object> parseJson(String json) {
        if (Config.USE_GSON) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(json, Map.class);
        } else {
            return jsonparser.JsonLib.readAsMap(json);
        }
    }
    
    private static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
