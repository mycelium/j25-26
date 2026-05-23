import httpserverlib.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("localhost", 3000, 4, false);

        server.addListener("/hello", HttpMethod.GET, (req, res) -> {
            res.setBody("Hello, World!");
        });

        server.addListener("/echo", HttpMethod.POST, (req, res) -> {
            res.setBody("Echo: " + new String(req.body));
        });

        server.addListener("/form", HttpMethod.POST, (req, res) -> {
            String name = req.multipartFields.getOrDefault("name", "unknown");
            res.setBody("Hello, " + name);
        });

        server.addListener("/data", HttpMethod.PUT, (req, res) -> {
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"status\":\"created\",\"data\":" + new String(req.body) + "}");
        });

        server.addListener("/data", HttpMethod.PATCH, (req, res) -> {
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"status\":\"updated\",\"data\":" + new String(req.body) + "}");
        });

        server.addListener("/data/{id}", HttpMethod.DELETE, (req, res) -> {
            res.setStatus(204);
            res.setBody("");
        });

        server.addListener("/data", HttpMethod.DELETE, (req, res) -> {
            res.setStatus(200);
            res.setBody("{\"status\":\"deleted\"}");
        });

        server.start();
    }
}