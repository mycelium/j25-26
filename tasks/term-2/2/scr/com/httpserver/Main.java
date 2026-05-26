package com.httpserver;

public class Main {
    public static void main(String[] args) {
        WebServerSettings config = new WebServerSettings()
                .setAddress("localhost")
                .setPortNumber(8081)
                .setPoolSize(10)
                .setVirtualMode(false);

        SimpleWebServer server = new SimpleWebServer(config);

        server.onGet("/hello", (req, res) -> res.withBody("Hello, World!"));

        server.onPost("/data", (req, res) -> res
                .withStatus(201, "Created")
                .withBody("Created: " + req.getBodyText()));

        server.onGet("/data", (req, res) -> res.withBody("All data"));

        server.onPut("/data/123", (req, res) -> res.withBody("Updated: " + req.getBodyText()));

        server.onPatch("/data/123", (req, res) -> res.withBody("Patched: " + req.getBodyText()));

        server.onDelete("/data/123", (req, res) -> res.withBody("Deleted"));

        server.launch();
    }
}