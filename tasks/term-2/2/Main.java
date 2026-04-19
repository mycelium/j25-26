import server.HttpServer;
import server.HttpServerBuilder;
import server.MultipartPart;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void runExample(){

        // Build server: fixed or virtual thread pool, configurable count
        HttpServer server = new HttpServerBuilder()
                .host("localhost")
                .port(8999)
                .threadCount(20)
                .useVirtualThreads(true)
                .build();

        // GET — simple HTML, no parameters
        server.addListener("/", HttpServer.HttpMethod.GET, (request, response) -> {
            response.setBody("<html><body><h1>Hello, World!</h1></body></html>")
                    .addHeader("Content-Type", "text/html");
        });

        // GET — path param {id}, access all headers, access query param
        // Example: GET /api/users/42?filter=active
        server.addListener("/api/users/{id}", HttpServer.HttpMethod.GET, (request, response) -> {
            String userId = request.getPathParams().get("id");

            // Headers are accessible as Map
            System.out.println("All headers: " + request.getHeaders());
            String accept = request.getHeader("accept");
            System.out.println("Accept header: " + accept);

            // Query params: ?filter=active
            String filter = request.getQueryParam("filter");

            String json = String.format(
                    "{\"id\":\"%s\",\"name\":\"User %s\",\"filter\":\"%s\"}",
                    userId, userId, filter);
            response.setBody(json).addHeader("Content-Type", "application/json");
        });

        // POST — read request body
        // Example: POST /api/data  body: {"key":"value"}
        server.addListener("/api/data", HttpServer.HttpMethod.POST, (request, response) -> {
            String body = request.getBodyAsString();
            System.out.println("Received POST body: " + body);

            response.setStatus(201, "Created")
                    .setBody("{\"status\":\"created\"}")
                    .addHeader("Content-Type", "application/json");
        });

        // PUT — full update of a resource
        // Example: PUT /api/data/42  body: {"name":"updated"}
        server.addListener("/api/data/{id}", HttpServer.HttpMethod.PUT, (request, response) -> {
            String id = request.getPathParams().get("id");
            String body = request.getBodyAsString();
            System.out.println("PUT resource " + id + ": " + body);

            response.setBody("{\"status\":\"updated\",\"id\":\"" + id + "\"}")
                    .addHeader("Content-Type", "application/json");
        });

        // PATCH — partial update of a resource
        // Example: PATCH /api/data/42  body: {"name":"patched"}
        server.addListener("/api/data/{id}", HttpServer.HttpMethod.PATCH, (request, response) -> {
            String id = request.getPathParams().get("id");
            String body = request.getBodyAsString();
            System.out.println("PATCH resource " + id + ": " + body);

            response.setBody("{\"status\":\"patched\",\"id\":\"" + id + "\"}")
                    .addHeader("Content-Type", "application/json");
        });

        // DELETE — remove a resource, 204 No Content
        // Example: DELETE /api/data/42
        server.addListener("/api/data/{id}", HttpServer.HttpMethod.DELETE, (request, response) -> {
            String id = request.getPathParams().get("id");
            System.out.println("DELETE resource " + id);

            response.setStatus(204, "No Content");
        });

        // POST multipart/form-data — bonus: parse parts (fields and files)
        // Example: POST /upload  Content-Type: multipart/form-data; boundary=----Boundary
        server.addListener("/upload", HttpServer.HttpMethod.POST, (request, response) -> {
            if (!request.isMultipart()) {
                response.setStatus(400, "Bad Request").setBody("Expected multipart/form-data");
                return;
            }

            List<MultipartPart> parts = request.getMultipartParts();
            StringBuilder info = new StringBuilder();
            for (MultipartPart part : parts) {
                String name = part.getName();
                String filename = part.getFilename();
                if (filename != null) {
                    System.out.printf("File field '%s': filename=%s, size=%d bytes, type=%s%n",
                            name, filename, part.getBody().length, part.getContentType());
                } else {
                    System.out.printf("Text field '%s': %s%n", name, part.getBodyAsString());
                }
                info.append(name).append(' ');
            }

            response.setBody("{\"status\":\"uploaded\",\"parts\":" + parts.size() + "}")
                    .addHeader("Content-Type", "application/json");
        });

        // Start server
        try {
            server.start();
            System.out.println("Server running. Press Enter to stop...");
            System.in.read();
            server.stop();
        } catch (IOException e) {
            System.err.println("Something bad really happend. Server stopped.");
        }

    }

    public static void main(String[] args) {

        runExample();
        
    }
}
