import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import com.google.gson.Gson;

import http.Engine;
import http.ReqMethod;
import jsonengine.JsonProcessor;

public class ServerApp {
    private static final int PORT = 12345;
    
    private static final boolean USE_VIRTUAL = false; 
    private static final boolean USE_GSON = false;
    private static final int THREADS = 16;

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        Engine server = new Engine("127.0.0.1", PORT, THREADS, USE_VIRTUAL);

        // Request 1
        server.route(ReqMethod.POST, "/req1", (req, res) -> {
            try {
                String json = req.getPayloadAsString();
                
                DataModel input = USE_GSON 
                    ? gson.fromJson(json, DataModel.class) 
                    : JsonProcessor.read(json, DataModel.class);

                Path path = Path.of("temp_" + UUID.randomUUID() + ".txt");
                Files.writeString(path, input.data);
                String content = Files.readString(path);
                Files.deleteIfExists(path);

                ResultModel result = new ResultModel(input.data, content);
                String outJson = USE_GSON ? gson.toJson(result) : JsonProcessor.write(result);

                res.addHeader("Content-Type", "application/json");
                res.setPayload(outJson);
                res.setStatusCode(200);
            } catch (Exception e) {
                res.setStatusCode(500);
                res.setPayload("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // Request 2
        server.route(ReqMethod.POST, "/req2", (req, res) -> {
            try {
                String json = req.getPayloadAsString();

                CalcModel input = USE_GSON 
                    ? gson.fromJson(json, CalcModel.class) 
                    : JsonProcessor.read(json, CalcModel.class);

                long sum = input.a + input.b;

                CalcResult result = new CalcResult(sum);
                String outJson = USE_GSON ? gson.toJson(result) : JsonProcessor.write(result);

                res.addHeader("Content-Type", "application/json");
                res.setPayload(outJson);
                res.setStatusCode(200);
            } catch (Exception e) {
                res.setStatusCode(500);
                res.setPayload("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        System.out.println("=== Performance Server Started ===");
        System.out.println("JSON Mode: " + (USE_GSON ? "GSON" : "Own Parser"));
        System.out.println("Threads: " + (USE_VIRTUAL ? "Virtual" : "Platform (" + THREADS + ")"));
        
        server.launch();
    }

    public static class DataModel { public String data; public DataModel() {} }
    public static class ResultModel { 
        public String saved; public String loaded; 
        public ResultModel() {} 
        public ResultModel(String s, String l) { saved = s; loaded = l; } 
    }
    public static class CalcModel { public long a; public long b; public CalcModel() {} }
    public static class CalcResult { 
        public long total; 
        public CalcResult() {} 
        public CalcResult(long r) { total = r; } 
    }
}