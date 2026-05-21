import httpserver.*;

public class Main {

    public static void main(String[] args) throws Exception {

        HttpServer server = new HttpServer("localhost", 8080, 4, false);

        server.addRoute(HttpMethod.GET, "/hello", (req, res) -> {
            res.setStatus(200)
               .setHeader("Content-Type", "text/plain")
               .setBody("Hello, World!");
        });

        server.addRoute(HttpMethod.GET, "/greet", (req, res) -> {
            String name = req.getQueryParam("name");
            if (name == null || name.isBlank()) name = "stranger";
            res.setStatus(200).setBody("Hello, " + name + "!");
        });

        server.addRoute(HttpMethod.POST, "/echo", (req, res) -> {
            System.out.println("Received headers: " + req.getHeaders());
            res.setStatus(200).setBody("Echo: " + req.getBodyAsString());
        });

        server.addRoute(HttpMethod.PUT, "/users/1", (req, res) -> {
            res.setStatus(200).setBody("PUT OK: " + req.getBodyAsString());
        });

        server.addRoute(HttpMethod.PATCH, "/users/1", (req, res) -> {
            res.setStatus(200).setBody("PATCH OK: " + req.getBodyAsString());
        });

        server.addRoute(HttpMethod.DELETE, "/users/1", (req, res) -> {
            res.setStatus(204);
        });

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.stop();
    }
}
