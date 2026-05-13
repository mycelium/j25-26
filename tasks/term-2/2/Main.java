import server.HttpMethod;
import server.HttpServer;

public class Main {

    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080, 4, false);

        server.addRoute(HttpMethod.GET, "/hello", (req, res) -> {
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"message\":\"hello world\"}");
        });

        server.addRoute(HttpMethod.POST, "/echo", (req, res) -> {
            res.setHeader("Content-Type", "text/plain");
            res.setBody(req.getBodyAsString());
        });

        server.addRoute(HttpMethod.GET, "/greet", (req, res) -> {
            String name = req.getQueryParams().getOrDefault("name", "stranger");
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"greeting\":\"Hello, " + name + "!\"}");
        });

        server.addRoute(HttpMethod.PUT, "/data", (req, res) -> {
            res.setStatus(200);
            res.setBody("Updated: " + req.getBodyAsString());
        });

        server.addRoute(HttpMethod.PATCH, "/data", (req, res) -> {
            res.setStatus(200);
            res.setBody("Patched: " + req.getBodyAsString());
        });

        server.addRoute(HttpMethod.DELETE, "/data", (req, res) -> {
            res.setStatus(200);
            res.setBody("Deleted");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
