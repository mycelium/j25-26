package com.loadtest;

import com.google.gson.Gson;
import com.httpserver.HttpServer;
import com.httpserver.ServerConfig;
import com.jsonparser.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Server {

    private static final int PORT = 8081;
    private static final int CLASSIC_POOL_SIZE = 50;
    private static final Path STORAGE = Path.of("storage");

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: Server <own|gson> <virtual|classic>");
            System.exit(1);
        }
        boolean useGson    = args[0].equals("gson");
        boolean useVirtual = args[1].equals("virtual");

        Files.createDirectories(STORAGE);
        Gson gson = new Gson();

        ServerConfig config = new ServerConfig()
                .host("0.0.0.0")
                .port(PORT)
                .threadCount(CLASSIC_POOL_SIZE)
                .useVirtualThreads(useVirtual);

        final HttpServer server = new HttpServer(config);

        server.post("/file", (req, res) -> {
            try {
                Map<String, Object> in = parse(req.getBody(), useGson, gson);
                String data = String.valueOf(in.get("data"));

                Path file = STORAGE.resolve(UUID.randomUUID() + ".txt");
                Files.writeString(file, data);
                String back = Files.readString(file);

                Map<String, Object> out = new LinkedHashMap<>();
                out.put("size", back.length());
                out.put("ok", back.equals(data));
                return res.header("Content-Type", "application/json")
                        .body(toJson(out, useGson, gson));
            } catch (Exception e) {
                return res.status(500, "Server Error").body(e.getMessage());
            }
        });

        server.post("/compute", (req, res) -> {
            try {
                Map<String, Object> in = parse(req.getBody(), useGson, gson);
                List<?> numbers = (List<?>) in.get("numbers");

                double sum = 0.0;
                for (Object n : numbers) sum += ((Number) n).doubleValue();
                double avg = sum / numbers.size();

                Map<String, Object> out = new LinkedHashMap<>();
                out.put("count", numbers.size());
                out.put("sum", sum);
                out.put("avg", avg);
                return res.header("Content-Type", "application/json")
                        .body(toJson(out, useGson, gson));
            } catch (Exception e) {
                return res.status(500, "Server Error").body(e.getMessage());
            }
        });

        server.get("/health", (req, res) -> res.body("ok"));

        server.post("/shutdown", (req, res) -> {
            new Thread(() -> {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                server.stop();
                System.exit(0);
            }, "shutdown").start();
            return res.body("stopping");
        });

        System.out.printf("Server: parser=%s, threads=%s, port=%d%n",
                args[0], args[1], PORT);
        server.start();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(String body, boolean useGson, Gson gson) {
        return useGson
                ? gson.fromJson(body, Map.class)
                : JsonParser.fromJsonToMap(body);
    }

    private static String toJson(Object value, boolean useGson, Gson gson) {
        return useGson ? gson.toJson(value) : JsonParser.toJson(value);
    }
}