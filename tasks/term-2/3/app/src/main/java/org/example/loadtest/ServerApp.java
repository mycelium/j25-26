package org.example.loadtest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.http.HttpMethod;
import org.example.http.HttpResponse;
import org.example.http.HttpServer;
import org.example.json.Json;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class ServerApp {

    private static final Gson GSON = new Gson();
    private static final Path DATA_FILE = Paths.get("load-test-data.txt");

    public static HttpServer buildServer(Config cfg) {
        return HttpServer.builder()
                .port(cfg.serverPort)
                .threads(cfg.serverThreads)
                .isVirtual(cfg.isVirtual)
                .route("/request1", HttpMethod.POST, req -> handleRequest1(req.getBody(), cfg.useOwnParser))
                .route("/request2", HttpMethod.POST, req -> handleRequest2(req.getBody(), cfg.useOwnParser))
                .build();
    }

    /**
     * Request 1: Parse JSON, store value in file, retrieve it, return result.
     */
    private static HttpResponse handleRequest1(String body, boolean useOwnParser) throws Exception {
        String id;
        String data;

        if (useOwnParser) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) Json.parse(body);
            id = String.valueOf(map.get("id"));
            data = String.valueOf(map.get("data"));
        } else {
            JsonObject obj = GSON.fromJson(body, JsonObject.class);
            id = obj.get("id").getAsString();
            data = obj.get("data").getAsString();
        }

        // Store to file
        String entry = id + "=" + data + System.lineSeparator();
        synchronized (ServerApp.class) {
            Files.writeString(DATA_FILE, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }

        // Retrieve last line from file
        String retrieved;
        synchronized (ServerApp.class) {
            String content = Files.readString(DATA_FILE);
            String[] lines = content.split(System.lineSeparator());
            retrieved = lines[lines.length - 1];
        }

        String responseJson;
        if (useOwnParser) {
            responseJson = Json.toJson(Map.of("stored", true, "retrieved", retrieved));
        } else {
            JsonObject resp = new JsonObject();
            resp.addProperty("stored", true);
            resp.addProperty("retrieved", retrieved);
            responseJson = GSON.toJson(resp);
        }

        return new HttpResponse().json(responseJson);
    }

    /**
     * Request 2: Parse JSON, calculate sum and average of values array, return result.
     */
    private static HttpResponse handleRequest2(String body, boolean useOwnParser) {
        double sum = 0;
        int count = 0;

        if (useOwnParser) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) Json.parse(body);
            @SuppressWarnings("unchecked")
            java.util.List<Object> values = (java.util.List<Object>) map.get("values");
            for (Object v : values) {
                sum += ((Number) v).doubleValue();
                count++;
            }
        } else {
            JsonObject obj = GSON.fromJson(body, JsonObject.class);
            for (var el : obj.getAsJsonArray("values")) {
                sum += el.getAsDouble();
                count++;
            }
        }

        double avg = count > 0 ? sum / count : 0;

        String responseJson;
        if (useOwnParser) {
            responseJson = Json.toJson(Map.of("sum", sum, "average", avg, "count", count));
        } else {
            JsonObject resp = new JsonObject();
            resp.addProperty("sum", sum);
            resp.addProperty("average", avg);
            resp.addProperty("count", count);
            responseJson = GSON.toJson(resp);
        }

        return new HttpResponse().json(responseJson);
    }
}
