public class Main {
    public static void main(String[] args) throws Exception {

        SimpleHttpServer server = new SimpleHttpServer("localhost", 8080, 10);

        server.get("/hello", (request, response) -> {
            response.send("Hello, World!");
        });

        server.get("/user", (request, response) -> {
            String name = request.getParam("name");
            if (name == null) name = "Guest";
            response.send("Hello, " + name + "!");
        });

        server.post("/echo", (request, response) -> {
            String body = request.getBody();
            response.send("You sent: " + body);
        });

        server.get("/json", (request, response) -> {
            response.sendJson("{\"message\":\"Hello JSON!\",\"status\":\"ok\"}");
        });

        server.get("/html", (request, response) -> {
            response.sendHtml("<html><body><h1>Hello HTML!</h1></body></html>");
        });

        server.put("/update", (request, response) -> {
            response.send("Updated!");
        });

        server.delete("/delete", (request, response) -> {
            response.send("Deleted!");
        });

        server.get("/notfound", (request, response) -> {
            response.status(404);
            response.send("Page not found");
        });

        server.get("/headers", (request, response) -> {
            String userAgent = request.getHeader("user-agent");
            response.setHeader("X-Custom-Header", "Hello");
            response.send("Your User-Agent: " + userAgent);
        });

        server.start();
    }
}