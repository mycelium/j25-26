import java.util.List;

public class Main {
    public static void main(String[] args) {
        SimpleHttpServer server = new SimpleHttpServer("localhost", 8080, 10, false);
        server.route("GET", "/hello", (req, res) -> {
            String name = req.getQueryParams().getOrDefault("name", "Guest");
            res.setBody("Hello, " + name + "!");
        });

        server.route("POST", "/data", (req, res) -> {
            System.out.println("[POST] Received: " + req.getBodyAsString());
            res.setStatus(201, "Created");
            res.setBody("Data received successfully");
        });

        server.route("PUT", "/update", (req, res) -> {
            System.out.println("[PUT] Updated with: " + req.getBodyAsString());
            res.setBody("Resource fully updated");
        });

        server.route("PATCH", "/update", (req, res) -> {
            System.out.println("[PATCH] Patched with: " + req.getBodyAsString());
            res.setBody("Resource partially updated");
        });

        server.route("DELETE", "/remove", (req, res) -> {
            System.out.println("[DELETE] Resource deleted");
            res.setStatus(204, "No Content");
        });

        server.route("GET", "/info", (req, res) -> {
            String userAgent = req.getHeaders().getOrDefault("User-Agent", "Unknown Browser");
            res.setHeader("X-Custom-Header", "MyJavaServer-v1");
            res.setBody("Your Browser: " + userAgent);
        });

        server.route("GET", "/long", (req, res) -> {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println("[LONG] Processing in thread: " + threadName);
                Thread.sleep(3000); 
                res.setBody("Done! Handled by: " + threadName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        server.route("POST", "/upload", (req, res) -> {
            List<FormPart> parts = req.getFormParts();
            
            if (parts.isEmpty()) {
                res.setStatus(400, "Bad Request");
                res.setBody("No multipart data found");
                return;
            }

            StringBuilder responseBuilder = new StringBuilder();
            for (FormPart part : parts) {
                if (part.getFileName() != null) {
                    responseBuilder.append("File: ")
                                   .append(part.getFileName())
                                   .append(", size: ")
                                   .append(part.getContent().length)
                                   .append(" bytes\n");
                } else {
                    responseBuilder.append("Field: ")
                                   .append(part.getName())
                                   .append(" = ")
                                   .append(part.getValue())
                                   .append("\n");
                }
            }
            
            res.setBody(responseBuilder.toString());
        });

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
