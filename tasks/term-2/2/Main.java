import httpserver.HttpServer;
import httpserver.RequestMethod;

import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("127.0.0.1", 9876, 4, false);

        Map<String, String> storage = new HashMap<>();

        server.addRoute("/api/status", RequestMethod.GET, (req, res) -> {
            res.setBody("Server is running!");
        });

        server.addRoute("/api/mirror", RequestMethod.POST, (req, res) -> {
            res.setBody("You sent: " + req.getBodyAsString());
        });

        server.addRoute("/api/upload", RequestMethod.POST, (req, res) -> {
            String user = req.getFormData().getOrDefault("username", "Guest");
            res.setBody("Welcome to the system, " + user);
        });

        server.addRoute("/api/form", RequestMethod.POST, (req, res) -> {
            Map<String, String> formData = req.getFormData();
            String name = formData.getOrDefault("name", "Unknown");
            String email = formData.getOrDefault("email", "Unknown");

            res.setHeader("Content-Type", "application/json");
            res.setBody(String.format("{\"name\": \"%s\", \"email\": \"%s\"}", name, email));
        });

        server.addRoute("/api/storage", RequestMethod.DELETE, (req, res) -> {
            String key = req.getQueryParams().get("key");
            storage.remove(key);
            res.setHeader("Content-Type", "application/json");
            res.setBody(String.format("{\"status\": \"deleted\", \"key\": \"%s\"}", key));
        });

        server.addRoute("/api/storage", RequestMethod.PATCH, (req, res) -> {
            String key = req.getQueryParams().get("key");
            String value = req.getBodyAsString();
            storage.put(key, value);
            res.setHeader("Content-Type", "application/json");
            res.setBody(String.format("{\"status\": \"updated\", \"key\": \"%s\"}", key));
        });

        server.addRoute("/api/storage", RequestMethod.GET, (req, res) -> {
            String key = req.getQueryParams().get("key");
            if (key != null) {
                String value = storage.getOrDefault(key, "not found");
                res.setHeader("Content-Type", "application/json");
                res.setBody(String.format("{\"%s\": \"%s\"}", key, value));
            } else {
                res.setHeader("Content-Type", "application/json");
                res.setBody(storage.toString());
            }
        });

        server.start();
    }
}