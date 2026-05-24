package lab3;

import com.google.gson.Gson;
import lab1.json.Json;
import lab2.http.ServerResponse;
import lab2.http.WebEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerformanceNode {

    private static final Gson gsonClient = new Gson();
    private static final Json nativeJson = new Json();

    public static void main(String[] args) {
        boolean enableVirtual = args.length > 0 ? Boolean.parseBoolean(args[0]) : true;
        boolean enableGson = args.length > 1 ? Boolean.parseBoolean(args[1]) : false;
        
        System.out.println("Starting PerformanceNode with VirtualThreads: " + enableVirtual + ", GSON: " + enableGson);

        WebEngine node = new WebEngine("127.0.0.1", 8083, 250, enableVirtual);

        node.registerRoute("POST", "/api/v1/disk-task", req -> {
            try {
                String payloadStr = req.getTextBody();
                Map<?, ?> parsedContent;

                if (enableGson) {
                    parsedContent = gsonClient.fromJson(payloadStr, Map.class);
                } else {
                    parsedContent = nativeJson.parseToMap(payloadStr);
                }

                String extractedValue = String.valueOf(parsedContent.get("identifier"));
                String diskData;

                Path tempPath = Path.of("io_storage_" + UUID.randomUUID() + ".dat");
                Files.writeString(tempPath, extractedValue);
                diskData = Files.readString(tempPath);
                Files.deleteIfExists(tempPath);

                return new ServerResponse(200, "OK", "{\"status\":\"ok\", \"retrieved\":\"" + diskData + "\"}");
            } catch (Exception ex) {
                return new ServerResponse(500, "Error", "{\"error\":\"io_failure\"}");
            }
        });

        node.registerRoute("POST", "/api/v1/math-task", req -> {
            try {
                String payloadStr = req.getTextBody();
                Map<?, ?> parsedContent;

                if (enableGson) {
                    parsedContent = gsonClient.fromJson(payloadStr, Map.class);
                } else {
                    parsedContent = nativeJson.parseToMap(payloadStr);
                }

                Object cyclesObj = parsedContent.get("cycles");
                double threshold = Double.parseDouble(cyclesObj != null ? String.valueOf(cyclesObj) : "5000");
                long accumulation = 0;
                for (int k = 0; k < threshold; k++) {
                    accumulation += (k * 2) - 1;
                }

                Map<String, Object> outputMap = new HashMap<>();
                outputMap.put("operation", "success");
                outputMap.put("result", accumulation);

                String finalJson = enableGson ? gsonClient.toJson(outputMap) : nativeJson.toJson(outputMap);
                return new ServerResponse(200, "OK", finalJson);
            } catch (Exception ex) {
                return new ServerResponse(500, "Error", "{\"error\":\"calc_failure\"}");
            }
        });

        node.start();
    }
}