package com.httpserver;

public class Main {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig()
                .host("localhost")
                .port(8081)
                .threadCount(10)
                .useVirtualThreads(false);

        HttpServer server = new HttpServer(config);

        server.get("/hello", (req, res) -> res.body("Hello, World!"));

        server.post("/data", (req, res) -> res.status(201, "Created").body("Created: " + req.getBody()));

        server.get("/data", (req, res) -> res.body("All data"));

        server.put("/data/123", (req, res) -> res.body("Updated: " + req.getBody()));

        server.patch("/data/123", (req, res) -> res.body("Patched: " + req.getBody()));

        server.delete("/data/123", (req, res) -> res.body("Deleted"));

        server.start();
    }
}