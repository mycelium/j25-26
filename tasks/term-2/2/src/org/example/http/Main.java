package org.example.http;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer("localhost", 8082, 10, false);

        server.addRoute("/hello", HttpMethod.GET, (req, res) -> {
            res.setStatus(200, "OK");
            res.setBody("Hello, World!");
        });

        server.addRoute("/echo", HttpMethod.POST, (req, res) -> {
            String body = req.getBodyAsString();
            res.setHeader("Content-Type", "text/plain");
            res.setStatus(200, "OK");
            res.setBody("Received: " + body);
        });

        server.addRoute("/api/status", HttpMethod.GET, (req, res) -> {
            res.setJsonBody("{\"status\": \"running\", \"uptime\": \"100%\"}");
        });

        server.addRoute("/update", HttpMethod.PUT, (req, res) -> {
            res.setStatus(200, "OK");
            res.setBody("Resource updated");
        });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}