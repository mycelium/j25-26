import http.*;

import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.builder()
                .host("localhost")
                .port(55555)
                .threads(4)
                .virtual(false)
                .build();

        server.on(HttpMethod.GET, "/hello", (req, res) -> {
            res.writeText("Hello, World!");
        });

        server.on(HttpMethod.GET, "/greet", (req, res) -> {
            String name = req.query().getOrDefault("name", "stranger");
            res.writeText("Hello, " + name + "!");
        });

        server.on(HttpMethod.POST, "/echo", (req, res) -> {
            res.writeText("Echo: " + req.bodyAsString());
        });

        server.on(HttpMethod.PUT, "/items", (req, res) -> {
            res.status(201).writeText("Item created: " + req.bodyAsString());
        });

        server.on(HttpMethod.PATCH, "/items", (req, res) -> {
            res.writeText("Item patched: " + req.bodyAsString());
        });

        server.on(HttpMethod.DELETE, "/items", (req, res) -> {
            res.status(200).writeText("Item deleted");
        });

        server.on(HttpMethod.GET, "/headers", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> h : req.headers().entrySet()) {
                sb.append(h.getKey()).append(": ").append(h.getValue()).append('\n');
            }
            res.writeText(sb.toString());
        });

        server.on(HttpMethod.POST, "/form", (req, res) -> {
            String name = req.formFields().getOrDefault("name", "unknown");
            res.writeText("Hello, " + name);
        });

        server.on(HttpMethod.POST, "/upload", (req, res) -> {
            MultipartPart file = req.parts().get("file");
            if (file == null) {
                res.status(400).writeText("File field is missing");
                return;
            }

            res.writeText("Uploaded: " + file.filename() + ", size=" + file.body().length);
        });

        server.start();

        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.stop();
    }
}
