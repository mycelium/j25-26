package com.httpserver;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(8081, 10);

        server.get("/hello", (req, res) -> {
            return res.body("Hello, World!");
        });

        server.post("/data", (req, res) -> {
            return res.status(201, "Created").body("Created: " + req.getBody());
        });

        server.get("/data", (req, res) -> {
            return res.body("All data");
        });

        server.put("/data/123", (req, res) -> {
            return res.body("Updated: " + req.getBody());
        });

        server.delete("/data/123", (req, res) -> {
            return res.body("Deleted");
        });

        server.start();
    }
}