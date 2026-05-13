package org.api;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        ServerConfig config = ServerConfig.builder()
                .host("localhost")
                .port(8080)
                .threadCount(4)
                .isVirtual(false)
                .build();

        HttpServer server = new HttpServer(config);

        server.get("/hello", req ->
                HttpResponse.ok().body("Hello, World!")
        );

        server.get("/greet", req -> {
            String name = req.queryParam("name");
            return HttpResponse.ok().body("Hello, " + (name != null ? name : "stranger") + "!");
        });

        server.post("/echo", req ->
                HttpResponse.ok().body(req.bodyAsString())
        );

        server.post("/json", req ->
                HttpResponse.ok().json("{\"received\": " + req.bodyAsString().length() + "}")
        );

        server.put("/update", req ->
                HttpResponse.ok().body("Updated: " + req.bodyAsString())
        );

        server.patch("/patch", req ->
                HttpResponse.ok().body("Patched: " + req.bodyAsString())
        );

        server.delete("/item", req -> {
            String id = req.queryParam("id");
            return HttpResponse.ok().body("Deleted item " + id);
        });

        server.post("/upload", req -> {
            List<MultipartPart> parts = req.parts();
            if (parts.isEmpty())
                return HttpResponse.badRequest().body("No multipart parts found");

            StringBuilder sb = new StringBuilder("Received parts:\n");
            for (MultipartPart part : parts) {
                sb.append("  name=").append(part.name());
                if (part.filename() != null) sb.append(", file=").append(part.filename());
                sb.append(", size=").append(part.body().length).append(" bytes\n");
            }
            return HttpResponse.ok().body(sb.toString());
        });

        server.get("/headers", req -> {
            StringBuilder sb = new StringBuilder();
            req.headers().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
            return HttpResponse.ok().body(sb.toString());
        });

        server.addRoute(HttpMethod.GET, "/status", req ->
                HttpResponse.ok().json("{\"status\": \"up\"}")
        );

        server.start();

        System.out.println("Server running on http://localhost:8080 — press Enter to stop.");
        System.in.read();

        server.stop();
    }
}
