package com.labs.server;

import httpserverlib.*;
import com.labs.config.ParserMode;
import com.labs.json.JsonAdapter;
import java.io.IOException;
import java.util.Map;
import httpserverlib.Request;
import httpserverlib.Response;

public class TestServer {
    private final HttpServer server;
    private final String host;
    private final int port;
    private final ParserMode parserMode;
    private final Storage storage;

    public TestServer(String host, int port, int threadPoolSize, boolean isVirtual, ParserMode parserMode) {
        this.parserMode = parserMode;
        this.host = host;
        this.port = port;
        this.storage = new Storage("test.db");
        this.server = new HttpServer(host, port, threadPoolSize, isVirtual);
        setupRoutes();
    }

    private void setupRoutes() {
        server.addListener("/request1", HttpMethod.POST, (req, res) -> {
            try {
                Map<String, Object> input = JsonAdapter.parse(req.body, parserMode);
                String key = (String) input.get("key");
                String value = (String) input.get("value");

                storage.save(key, value);

                String retrieved = storage.load(key);

                Map<String, String> response = Map.of("retrieved", retrieved);
                String json = JsonAdapter.toJson(response, parserMode);

                res.setHeader("Content-Type", "application/json");
                res.setBody(json);
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.addListener("/request2", HttpMethod.POST, (req, res) -> {
            try {
                Map<String, Object> input = JsonAdapter.parse(req.body, parserMode);
                Number num = (Number) input.get("number");
                long n = num.longValue();

                long result = n * n + n * 31 + System.nanoTime() % 100;

                Map<String, Long> response = Map.of("result", result);
                String json = JsonAdapter.toJson(response, parserMode);

                res.setHeader("Content-Type", "application/json");
                res.setBody(json);
            } catch (Exception e) {
                res.setStatus(500);
                res.setBody("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Cервер запущен на " + host + ":" + port);
    }

    public void stop() {
        try {
            server.stop();
        } catch (IOException e) {
            System.err.println("Ошибка при остановке сервера: " + e.getMessage());
        }
        storage.close();
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
}