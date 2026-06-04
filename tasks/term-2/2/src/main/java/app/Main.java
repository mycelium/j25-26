package app;

import httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("localhost", 8080, 4, false);

        server.addRoute("/", "GET", (req, resp) -> {
            resp.setBody("Home page");
        });

        server.addRoute("/hello", "GET", (req, resp) -> {
            String name = req.getQueryParams().getOrDefault("name", "World");
            resp.setBody("Hello, " + name + "!");
        });

        server.addRoute("/data", "POST", (req, resp) -> {
            resp.setBody("Received: " + req.getBodyAsString());
        });

        server.start();
        System.out.println("Server running on http://localhost:8080");
        // Чтобы сервер не завершился сразу, ждем нажатия Enter
        System.in.read();
        server.stop();
    }
}