import server.*;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServerBuilder()
                .host("localhost")
                .port(8080)
                .threadCount(10)
                .isVirtual(false)
                .build();

        server.addListener("/", HttpMethod.GET, (req, res) -> {
            res.setBody("<html><body><h1>Hello!</h1></body></html>")
               .addHeader("Content-Type", "text/html");
        });

        server.addListener("/api/users/{id}", HttpMethod.GET, (req, res) -> {
            String id = req.getPathParams().get("id");
            String filter = req.getQueryParam("filter");

            System.out.println("Headers: " + req.getHeaders());
            System.out.println("Accept: " + req.getHeader("accept"));

            res.setBody("{\"id\":\"" + id + "\",\"filter\":\"" + filter + "\"}")
               .addHeader("Content-Type", "application/json");
        });

        server.addListener("/api/data", HttpMethod.POST, (req, res) -> {
            System.out.println("Body: " + req.getBodyAsString());
            res.setStatus(201, "Created")
               .setBody("{\"status\":\"created\"}")
               .addHeader("Content-Type", "application/json");
        });

        server.addListener("/api/data/{id}", HttpMethod.PUT, (req, res) -> {
            String id = req.getPathParams().get("id");
            res.setBody("{\"status\":\"updated\",\"id\":\"" + id + "\"}")
               .addHeader("Content-Type", "application/json");
        });

        server.addListener("/api/data/{id}", HttpMethod.PATCH, (req, res) -> {
            String id = req.getPathParams().get("id");
            res.setBody("{\"status\":\"patched\",\"id\":\"" + id + "\"}")
               .addHeader("Content-Type", "application/json");
        });

        server.addListener("/api/data/{id}", HttpMethod.DELETE, (req, res) -> {
            String id = req.getPathParams().get("id");
            System.out.println("Deleting: " + id);
            res.setStatus(204, "No Content");
        });

        server.addListener("/upload", HttpMethod.POST, (req, res) -> {
            if (!req.isMultipart()) {
                res.setStatus(400, "Bad Request").setBody("Expected multipart/form-data");
                return;
            }

            List<MultipartPart> parts = req.getMultipartParts();
            for (MultipartPart part : parts) {
                if (part.getFilename() != null) {
                    System.out.printf("File '%s': %s (%d bytes)%n",
                            part.getName(), part.getFilename(), part.getBody().length);
                } else {
                    System.out.printf("Field '%s': %s%n", part.getName(), part.getBodyAsString());
                }
            }

            res.setBody("{\"status\":\"uploaded\",\"parts\":" + parts.size() + "}")
               .addHeader("Content-Type", "application/json");
        });

        server.start();

        System.out.println("Press Enter to stop...");
        System.in.read();

        server.stop();
    }
}
