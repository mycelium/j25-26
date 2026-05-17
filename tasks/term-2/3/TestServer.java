import com.google.gson.Gson;
import http.HttpResponse;
import http.HttpServer;
import json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TestServer {

    private static final Gson gson = new Gson();
    private static final JsonMapper myMapper = new JsonMapper();
    private static final Path TEMP_FILE = Path.of("temp_db.txt");

    public static void main(String[] args) {
        boolean useVirtual = args.length < 1 || args[0].equalsIgnoreCase("virtual");
        boolean useGson    = args.length >= 2 && args[1].equalsIgnoreCase("gson");
        int port           = args.length >= 3 ? Integer.parseInt(args[2]) : 8082;
        int threads        = args.length >= 4 ? Integer.parseInt(args[3]) : 200;

        System.out.println("--- Server Configuration ---");
        System.out.println("Threads : " + (useVirtual ? "Virtual" : "Classic (count=" + threads + ")"));
        System.out.println("Parser  : " + (useGson ? "GSON" : "Own Parser"));
        System.out.println("Port    : " + port);
        System.out.println("----------------------------");

        HttpServer server = new HttpServer("localhost", port, threads, useVirtual);

        // Request 1: parse JSON, write to file, read back from file (I/O bound)
        server.addHandler("POST", "/req1", request -> {
            try {
                String reqBody = request.getBody();
                if (reqBody == null || reqBody.isEmpty())
                    return new HttpResponse(400, "Bad Request", "Empty body");

                Map<String, Object> data = useGson
                    ? gson.fromJson(reqBody, Map.class)
                    : myMapper.fromJsonAsMap(reqBody);

                String textToSave = String.valueOf(data.get("payload"));
                String readData;
                synchronized (TestServer.class) {
                    Files.writeString(TEMP_FILE, textToSave);
                    readData = Files.readString(TEMP_FILE);
                }

                return new HttpResponse(200, "OK", "{\"status\":\"saved\",\"data\":\"" + readData + "\"}");
            } catch (Exception e) {
                return new HttpResponse(500, "Error", "Internal server error: " + e.getMessage());
            }
        });

        // Request 2: parse JSON, compute sum in memory, return JSON (CPU bound)
        server.addHandler("POST", "/req2", request -> {
            try {
                String reqBody = request.getBody();
                Map<String, Object> data = useGson
                    ? gson.fromJson(reqBody, Map.class)
                    : myMapper.fromJsonAsMap(reqBody);

                double limit = (Double) data.getOrDefault("limit", 1000.0);
                long sum = 0;
                for (long i = 0; i < (long) limit; i++) sum += i;

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("status", "success");
                responseMap.put("calculatedSum", sum);

                String responseJson = useGson ? gson.toJson(responseMap) : myMapper.toJson(responseMap);
                return new HttpResponse(200, "OK", responseJson);
            } catch (Exception e) {
                return new HttpResponse(500, "Error", "Internal server error: " + e.getMessage());
            }
        });

        server.start();
    }
}
