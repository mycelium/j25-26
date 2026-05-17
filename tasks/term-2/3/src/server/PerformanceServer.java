package server;

import com.google.gson.Gson;
import httpserver.HttpMethod;
import httpserver.HttpServer;
import json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceServer {

    private static final Gson GSON = new Gson();

    private static final ConcurrentHashMap<Long, Long> SUM_CACHE = new ConcurrentHashMap<>();

    private static boolean useGson;

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: PerformanceServer <port> <virtual|classic> <own|gson> <poolSize>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        boolean isVirtual = "virtual".equalsIgnoreCase(args[1]);
        useGson = "gson".equalsIgnoreCase(args[2]);
        int poolSize = Integer.parseInt(args[3]);

        HttpServer server = new HttpServer("0.0.0.0", port, poolSize, isVirtual);

        server.addHandler("/io", HttpMethod.POST, (req, res) -> {
            Map<String, Object> body = parseJson(req.getBodyAsString());
            String data = String.valueOf(body.get("data"));

            Path tmp = Files.createTempFile("perf-" + UUID.randomUUID(), ".tmp");
            try {
                Files.writeString(tmp, data);
                String retrieved = Files.readString(tmp);
                res.setJson(toJson(Map.of("stored", data, "retrieved", retrieved)));
            } finally {
                Files.deleteIfExists(tmp);
            }
        });

        server.addHandler("/compute", HttpMethod.POST, (req, res) -> {
            Map<String, Object> body = parseJson(req.getBodyAsString());
            long a = ((Number) body.get("a")).longValue();

            long result = SUM_CACHE.computeIfAbsent(a, n -> {
                long sum = 0L;
                for (long i = 1; i <= n; i++) sum += i * i;
                return sum;
            });

            res.setJson(toJson(Map.of("result", result)));
        });

        System.out.printf("Server started on :%d  [threads=%s  parser=%s  poolSize=%d]%n",
                port,
                isVirtual ? "virtual" : "classic",
                useGson ? "gson" : "own",
                poolSize);

        server.start();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json) {
        if (useGson) {
            return (Map<String, Object>) GSON.fromJson(json, Map.class);
        }
        return Json.parseMap(json);
    }

    private static String toJson(Object obj) {
        if (useGson) {
            return GSON.toJson(obj);
        }
        return Json.toJson(obj);
    }
}
