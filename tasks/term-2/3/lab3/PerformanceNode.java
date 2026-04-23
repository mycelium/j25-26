package lab3;

import com.google.gson.Gson;
import lab1.json.Json;
import lab2.http.ServerResponse;
import lab2.http.WebEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PerformanceNode {

    private static final boolean ENABLE_VIRTUAL = true;
    private static final boolean ENABLE_GSON = false;

    private static final Gson gsonClient = new Gson();
    private static final Json nativeJson = new Json();
    private static final Path STORAGE_PATH = Path.of("io_storage.dat");

    public static void main(String[] args) {
        WebEngine node = new WebEngine("127.0.0.1", 8083, 250, ENABLE_VIRTUAL);

        node.registerRoute("POST", "/api/v1/disk-task", req -> {
            try {
                String payloadStr = req.getTextBody();
                Map<?, ?> parsedContent;

                if (ENABLE_GSON) {
                    parsedContent = gsonClient.fromJson(payloadStr, Map.class);
                } else {
                    parsedContent = nativeJson.parseToMap(payloadStr);
                }

                String extractedValue = String.valueOf(parsedContent.get("identifier"));
                String diskData;

                synchronized (PerformanceNode.class) {
                    Files.writeString(STORAGE_PATH, extractedValue);
                    diskData = Files.readString(STORAGE_PATH);
                }

                return new ServerResponse(200, "OK", "{\"status\":\"ok\", \"retrieved\":\"" + diskData + "\"}");
            } catch (Exception ex) {
                return new ServerResponse(500, "Error", "{\"error\":\"io_failure\"}");
            }
        });

        node.registerRoute("POST", "/api/v1/math-task", req -> {
            try {
                String payloadStr = req.getTextBody();
                Map<?, ?> parsedContent;

                if (ENABLE_GSON) {
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

                String finalJson = ENABLE_GSON ? gsonClient.toJson(outputMap) : nativeJson.toJson(outputMap);
                return new ServerResponse(200, "OK", finalJson);
            } catch (Exception ex) {
                return new ServerResponse(500, "Error", "{\"error\":\"calc_failure\"}");
            }
        });

        node.launch();
    }
}