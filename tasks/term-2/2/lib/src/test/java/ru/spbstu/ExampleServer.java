package ru.spbstu;

import com.httpserverlib.HttpServer;
import com.httpserverlib.model.HttpResponse;
import com.httpserverlib.model.Part;

public class ExampleServer {

    public static void main(String[] args) throws Exception {
        // Create and configure the server
        HttpServer server = HttpServer.create()
                .host("localhost")
                .port(8080)
                .threadPoolSize(4)
                .useVirtualThreads(true)   // requires Java 21+
                .build();

        // ------------------------------------------------------------
        // 1. GET endpoint – with query parameters and custom headers
        // ------------------------------------------------------------
        server.get("/greet", req -> {
            String name = req.getQueryParam("name");
            if (name == null || name.isBlank()) {
                name = "World";
            }
            String lang = req.getHeader("Accept-Language");
            String greeting;
            if (lang != null && lang.startsWith("ru")) {
                greeting = "Привет, " + name + "!";
            } else {
                greeting = "Hello, " + name + "!";
            }
            return HttpResponse.ok(greeting)
                    .header("X-Custom-Header", "greeting-sent");
        });

        // ------------------------------------------------------------
        // 2. POST endpoint – reads JSON body and echoes as JSON
        // ------------------------------------------------------------
        server.post("/echo", req -> {
            String body = req.getBodyAsString();
            // Build a JSON response (manual escaping for simplicity)
            String json = "{\"received\": \"" + escapeJson(body) + "\"}";
            return HttpResponse.ok().json(json);
        });

        // ------------------------------------------------------------
        // 3. POST endpoint – plain text body, returns uppercase
        // ------------------------------------------------------------
        server.post("/uppercase", req -> {
            String upper = req.getBodyAsString().toUpperCase();
            return HttpResponse.ok(upper);
        });

        // ------------------------------------------------------------
        // 4. POST endpoint – multipart/form-data (fields + files)
        // ------------------------------------------------------------
        server.post("/upload", req -> {
            StringBuilder sb = new StringBuilder();
            for (Part part : req.multipartParts()) {
                sb.append("Part: ").append(part.name());
                if (part.isFile()) {
                    sb.append(" (file: ").append(part.filename())
                            .append(", size: ").append(part.getContent().length).append(" bytes)");
                } else {
                    sb.append(" = ").append(part.getContentAsString());
                }
                sb.append("\n");
            }
            if (sb.isEmpty()) {
                return HttpResponse.badRequest().body("No multipart parts found");
            }
            return HttpResponse.ok(sb.toString());
        });

        // ------------------------------------------------------------
        // 5. PUT endpoint – updates resource (demo)
        // ------------------------------------------------------------
        server.put("/resource", req -> {
            String id = req.getQueryParam("id");
            if (id == null) {
                return HttpResponse.badRequest().body("Missing 'id' query parameter");
            }
            return HttpResponse.ok("Resource " + id + " updated");
        });

        // ------------------------------------------------------------
        // 6. PATCH endpoint – partial update
        // ------------------------------------------------------------
        server.patch("/resource", req -> {
            String id = req.getQueryParam("id");
            String patchData = req.getBodyAsString();
            if (id == null) {
                return HttpResponse.badRequest().body("Missing 'id' query parameter");
            }
            return HttpResponse.ok("Patched resource " + id + " with: " + patchData);
        });

        // ------------------------------------------------------------
        // 7. DELETE endpoint
        // ------------------------------------------------------------
        server.delete("/resource", req -> {
            String id = req.getQueryParam("id");
            if (id == null) {
                return HttpResponse.badRequest().body("Missing 'id' query parameter");
            }
            return HttpResponse.noContent();   // 204 No Content
        });

        // ------------------------------------------------------------
        // 8. Endpoint that returns custom status code (201 Created)
        // ------------------------------------------------------------
        server.post("/items", req -> {
            // Simulate creation
            return HttpResponse.created().header("Location", "/items/123");
        });

        // ------------------------------------------------------------
        // 9. Endpoint that reads request headers and returns them
        // ------------------------------------------------------------
        server.get("/headers", req -> {
            StringBuilder sb = new StringBuilder("Headers received:\n");
            for (var entry : req.headers().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return HttpResponse.ok(sb.toString());
        });

        // ------------------------------------------------------------
        // 10. Endpoint that returns 404 for non‑existent path (implicit)
        // ------------------------------------------------------------
        // (No handler registered for /not-there → automatic 404)

        // Start the server (blocks)
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Available endpoints:");
        System.out.println("  GET    /greet?name=xxx");
        System.out.println("  POST   /echo          (JSON body)");
        System.out.println("  POST   /uppercase     (plain text)");
        System.out.println("  POST   /upload        (multipart/form-data)");
        System.out.println("  PUT    /resource?id=xxx");
        System.out.println("  PATCH  /resource?id=xxx");
        System.out.println("  DELETE /resource?id=xxx");
        System.out.println("  POST   /items");
        System.out.println("  GET    /headers");
        System.out.println("\nPress Ctrl+C to stop.");
        server.start();
    }

    // Simple JSON string escaping (avoids external libraries)
    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}