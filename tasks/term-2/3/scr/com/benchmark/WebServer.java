package com.benchmark;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.httpserver.HttpServer;
import com.httpserver.ServerConfig;
import com.jsonparser.JsonParser;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebServer {

    private static final int PORT = 8081;
    private static final int POOL_SIZE = 50;
    private static final Path STORAGE = Path.of("storage");

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: WebServer <custom|gson> <virtual|classic>");
            System.exit(1);
        }

        boolean useGson = args[0].equals("gson");
        boolean useVirtual = args[1].equals("virtual");

        Files.createDirectories(STORAGE);
        Gson gson = new Gson();

        ServerConfig config = new ServerConfig()
                .host("0.0.0.0")
                .port(PORT)
                .threadCount(POOL_SIZE)
                .useVirtualThreads(useVirtual);

        HttpServer server = new HttpServer(config);

        // Request 1: file I/O
        server.post("/file", (req, res) -> {
            try {
                Map<String, Object> data = parse(req.getBody(), useGson, gson);
                String content = String.valueOf(data.get("data"));

                Path file = STORAGE.resolve(UUID.randomUUID() + ".txt");
                Files.writeString(file, content);
                String restored = Files.readString(file);

                Map<String, Object> out = new HashMap<>();
                out.put("size", restored.length());
                out.put("matched", restored.equals(content));

                return res.header("Content-Type", "application/json")
                        .body(toJson(out, useGson, gson));
            } catch (Exception e) {
                return res.status(500, "Server Error").body(e.getMessage());
            }
        });

        // Request 2: computation
        server.post("/compute", (req, res) -> {
            try {
                Map<String, Object> data = parse(req.getBody(), useGson, gson);

                @SuppressWarnings("unchecked")
                List<Number> numbers = (List<Number>) data.get("values");

                double sum = 0.0;
                for (Number n : numbers) {
                    sum += n.doubleValue();
                }
                double avg = sum / numbers.size();

                Map<String, Object> out = new HashMap<>();
                out.put("count", numbers.size());
                out.put("sum", sum);
                out.put("average", avg);

                return res.header("Content-Type", "application/json")
                        .body(toJson(out, useGson, gson));
            } catch (Exception e) {
                return res.status(500, "Server Error").body(e.getMessage());
            }
        });

        server.get("/health", (req, res) -> res.body("ok"));

        server.post("/shutdown", (req, res) -> {
            new Thread(() -> {
                try { Thread.sleep(100); } catch (Exception ignored) {}
                server.stop();
                System.exit(0);
            }).start();
            return res.body("stopping");
        });

        System.out.printf("Server: %s / %s on port %d%n", args[0], args[1], PORT);
        server.start();
    }

    private static Map<String, Object> parse(String body, boolean useGson, Gson gson) {
        if (useGson) {
            return gson.fromJson(body, MAP_TYPE);
        } else {
            return JsonParser.fromJsonToMap(body);
        }
    }

    private static String toJson(Object obj, boolean useGson, Gson gson) {
        if (useGson) {
            return gson.toJson(obj);
        } else {
            return JsonParser.toJson(obj);
        }
    }
}