import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;

import httpserverlib.*;
import jsonlib.*;

public class PerformanceServer {
    private static final int PORT = 55555;
    private static final boolean USE_VIRTUAL_THREADS = false;
    private static final boolean USE_GSON = true;
    private static final int THREAD_POOL_SIZE = 16;

    private static Gson gson = null;
    static {
        if (USE_GSON) {
            gson = new Gson();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer("localhost", PORT, THREAD_POOL_SIZE, USE_VIRTUAL_THREADS);

        server.addListener("/req1", HttpMethod.POST, (req, resp) -> {
            try {
                String body = new String(req.body, StandardCharsets.UTF_8);
                System.out.println("Request-1 body: " + body);

                Request1Data data;
                if (USE_GSON) {
                    data = gson.fromJson(body, Request1Data.class);
                } else {
                    data = Json.fromJson(body, Request1Data.class);
                }
                System.out.println("Parsed data: " + data.data);

                Path file = Path.of("storage.txt");
                Files.writeString(file, data.data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                String retrieved = Files.readString(file);
                Response1Result result = new Response1Result(data.data, retrieved);
                String jsonResp;
                if (USE_GSON) {
                    jsonResp = gson.toJson(result);
                } else {
                    jsonResp = Json.toJson(result);
                }
                resp.setHeader("Content-Type", "application/json");
                resp.setBody(jsonResp);
                resp.setStatus(200);
            } catch (Exception e) {
                System.err.println("Error in /req1:");
                e.printStackTrace();
                resp.setStatus(500);
                resp.setBody("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        });

        server.addListener("/req2", HttpMethod.POST, (req, resp) -> {
            try {
                String body = new String(req.body, StandardCharsets.UTF_8);
                System.out.println("Request-2 body: " + body);

                Request2Data d;
                if (USE_GSON) {
                    d = gson.fromJson(body, Request2Data.class);
                } else {
                    d = Json.fromJson(body, Request2Data.class);
                }
                long sum = d.a + d.b;
                Response2Result result = new Response2Result(sum);
                String jsonResp;
                if (USE_GSON) {
                    jsonResp = gson.toJson(result);
                } else {
                    jsonResp = Json.toJson(result);
                }
                resp.setHeader("Content-Type", "application/json");
                resp.setBody(jsonResp);
                resp.setStatus(200);
            } catch (Exception e) {
                System.err.println("Error in /req2:");
                e.printStackTrace();
                resp.setStatus(500);
                resp.setBody("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        });

        System.out.println("Server started on port " + PORT);
        System.out.println("JSON library: " + (USE_GSON ? "Gson" : "own parser"));
        System.out.println("Threads: " + (USE_VIRTUAL_THREADS ? "virtual" : "classic (" + THREAD_POOL_SIZE + ")"));
        server.start();
    }

    public static class Request1Data { public String data; public Request1Data() {} }
    public static class Response1Result { public String stored; public String retrieved; public Response1Result() {} public Response1Result(String s, String r) { stored=s; retrieved=r; } }
    public static class Request2Data { public long a; public long b; public Request2Data() {} }
    public static class Response2Result { public long result; public Response2Result() {} public Response2Result(long r) { result=r; } }
}