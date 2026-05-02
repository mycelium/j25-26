package httpserver;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer.Builder()
                .host("localhost")
                .port(8081)
                .threadCount(4)
                .isVirtual(false)
                .build();

        server.addRoute(HttpMethod.GET, "/hello", (req, res) ->
                res.body("Hello, World!"));

        server.addRoute(HttpMethod.GET, "/qs", (req, res) ->
                res.body("param = " + req.getQueryParam("name")));

        server.addRoute(HttpMethod.POST, "/echo", (req, res) ->
                res.body("body: " + req.getBody()));

        server.addRoute(HttpMethod.PUT, "/put", (req, res) ->
                res.body("put: " + req.getBody()));

        server.addRoute(HttpMethod.PATCH, "/patch", (req, res) ->
                res.body("patch: " + req.getBody()));

        server.addRoute(HttpMethod.DELETE, "/delete", (req, res) ->
                res.status(204, "No Content"));

        server.addRoute(HttpMethod.POST, "/upload", (req, res) -> {
            if (!req.isMultipart()) {
                res.status(400, "Bad Request").body("not multipart");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (HttpRequest.Part p : req.getMultipartParts()) {
                sb.append(p.getName()).append(" = ");
                if (p.isFile()) sb.append("[file: ").append(p.getFilename()).append(", ").append(p.getData().length).append(" bytes]");
                else sb.append(p.getBodyAsString());
                sb.append("\n");
            }
            res.body(sb.toString());
        });

        server.start();
        System.out.println("Server running on http://localhost:8080");
        server.awaitTermination();
    }
}