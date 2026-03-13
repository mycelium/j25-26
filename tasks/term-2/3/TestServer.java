import com.google.gson.Gson;
import http.HttpResponse;
import http.HttpServer;
import json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestServer {
    
    private static final boolean USE_VIRTUAL_THREADS = true; // true = Virtual, false = Classic
    private static final boolean USE_GSON = false;           // true = Gson, false = Own Parser

    private static final Gson gson = new Gson();
    private static final JsonMapper myMapper = new JsonMapper();
    private static final Path TEMP_FILE = Path.of("temp_db.txt");

    public static void main(String[] args) {
        // Инициализация сервера
        HttpServer server = new HttpServer("localhost", 8082, 200, USE_VIRTUAL_THREADS);

        System.out.println("--- Server Configuration ---");
        System.out.println("Threads: " + (USE_VIRTUAL_THREADS ? "Virtual" : "Classic"));
        System.out.println("Parser:  " + (USE_GSON ? "GSON" : "Own Parser"));
        System.out.println("----------------------------");

        // Request 1: Принять JSON, записать в файл, прочитать из файла
        server.addHandler("POST", "/req1", request -> {
            try {
                String reqBody = request.getBody();
                if (reqBody == null || reqBody.isEmpty()) {
                    return new HttpResponse(400, "Bad Request", "Empty body");
                }

                Map<String, Object> data;

      
                if (USE_GSON) {
                    data = gson.fromJson(reqBody, Map.class);
                } else {
                    data = myMapper.fromJsonAsMap(reqBody);
                }

                String textToSave = String.valueOf(data.get("payload"));
                String readData;

       
                synchronized (TestServer.class) {
                    Files.writeString(TEMP_FILE, textToSave);
                    readData = Files.readString(TEMP_FILE);
                }

                return new HttpResponse(200, "OK", "{\"status\":\"saved\", \"data\":\"" + readData + "\"}");
            } catch (Exception e) {
         
                e.printStackTrace();
                return new HttpResponse(500, "Error", "Internal server error: " + e.getMessage());
            }
        });


        server.addHandler("POST", "/req2", request -> {
            String reqBody = request.getBody();
            Map<String, Object> data;

       
            if (USE_GSON) {
                data = gson.fromJson(reqBody, Map.class);
            } else {
                data = myMapper.fromJsonAsMap(reqBody);
            }

  
            double limit = (Double) data.getOrDefault("limit", 1000.0);
            long sum = 0;
            for (int i = 0; i < limit; i++) {
                sum += i; 
            }

     
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", "success");
            responseMap.put("calculatedSum", sum);

            String responseJson = USE_GSON ? gson.toJson(responseMap) : myMapper.toJson(responseMap);
            return new HttpResponse(200, "OK", responseJson);
        });

        server.start();
    }
}