import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import httpserver.HttpServer;
import httpserver.RequestMethod;
import json.Json;

public class ServerApp {
    private static final int PORT = 9876;
    private static final boolean USE_VIRTUAL_THREADS = false;
    private static final boolean USE_GSON = true;
    private static final int THREAD_POOL_SIZE = 16;

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer("127.0.0.1", PORT, THREAD_POOL_SIZE, USE_VIRTUAL_THREADS);

        server.addRoute("/req1", RequestMethod.POST, (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Req1Data data = parse(body, Req1Data.class);

                Path file = Path.of("storage.txt");
                Files.writeString(file, data.data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                data.data = Files.readString(file);

                String json = toJson(data);
                res.setHeader("Content-Type", "application/json");
                res.setBody(json);
            } catch (Exception e) {
                res.setStatus(500, "Error");
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addRoute("/req2", RequestMethod.POST, (req, res) -> {
            try {
                String body = req.getBodyAsString();
                Req2Data data = parse(body, Req2Data.class);

                long result = data.a + data.b;

                String json = toJson(new Res2Data(result));
                res.setHeader("Content-Type", "application/json");
                res.setBody(json);
            } catch (Exception e) {
                res.setStatus(500, "Error");
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
        System.out.println("JSON: " + (USE_GSON ? "Gson" : "Own"));
        System.out.println("Threads: " + (USE_VIRTUAL_THREADS ? "Virtual" : "Platform(" + THREAD_POOL_SIZE + ")"));
        server.start();
    }

    private static <T> T parse(String json, Class<T> clazz) {
        return USE_GSON ? gson.fromJson(json, clazz) : Json.parseToObject(json, clazz);
    }

    private static String toJson(Object obj) {
        return USE_GSON ? gson.toJson(obj) : Json.toJson(obj);
    }

    public static class Req1Data {
        public String data;
    }
    public static class Res2Data {
        public long result;
        public Res2Data(long r) { result = r; }
    }
    public static class Req2Data {
        public long a;
        public long b;
    }
}