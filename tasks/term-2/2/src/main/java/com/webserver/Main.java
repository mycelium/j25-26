package com.webserver;

public class Main {
    public static void main(String[] args) {
        WebServerConfig config = new WebServerConfig()
                .host("localhost")
                .port(8081)
                .threadPoolSize(10)
                .virtualThreads(false);

        WebServer server = new WebServer(config);

        server.get("/hello", (req, res) ->
                res.body("Hello, World!"));

        server.post("/data", (req, res) ->
                res.status(201, "Created").body("Created: " + req.getBody()));

        server.get("/data", (req, res) ->
                res.body("All data"));

        server.put("/data/123", (req, res) ->
                res.body("Updated: " + req.getBody()));

        server.patch("/data/123", (req, res) ->
                res.body("Patched: " + req.getBody()));

        server.delete("/data/123", (req, res) ->
                res.body("Deleted"));

        server.start();
    }
}
