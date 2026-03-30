import java.util.List;
public class Main {
    public static void main(String[] args) {
        HttpServ server = new HttpServ("localhost", 8080, 10, false);

        // GET
        server.addListener("GET", "/hello", (req, res) -> {
            String name = req.getQueryParams().getOrDefault("name", "Guest");
            res.setBody("Hello, " + name + "!");
        });

        // POST
        server.addListener("POST", "/data", (req, res) -> {
            System.out.println("[POST] Body: " + req.getBody());
            res.setStatus(201, "Created");
            res.setBody("Data received successfully");
        });

        // PUT
        server.addListener("PUT", "/update", (req, res) -> {
            System.out.println("[PUT] Body: " + req.getBody());
            res.setBody("Resource fully updated");
        });

        // PATCH
        server.addListener("PATCH", "/update", (req, res) -> {
            System.out.println("[PATCH] Body: " + req.getBody());
            res.setBody("Resource partially updated");
        });

        // DELETE
        server.addListener("DELETE", "/remove", (req, res) -> {
            res.setStatus(204, "No Content");
            System.out.println("[DELETE] Resource deleted");
        });

        // Headers как Map
        server.addListener("GET", "/info", (req, res) -> {
            String userAgent = req.getHeaders().getOrDefault("User-Agent", "Unknown");
            res.addHeader("X-Custom-Header", "JavaServer-v1");
            res.setBody("Your Browser: " + userAgent);
        });

        // многопоточность
        server.addListener("GET", "/long", (req, res) -> {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println("[LONG] Thread: " + threadName);
                Thread.sleep(3000);
                res.setBody("Done! Handled by: " + threadName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
     // Multipart form data
        server.addListener("POST", "/upload", (req, res) -> {
            List<MultipartPart> parts = req.getParts();

            if (parts.isEmpty()) {
                res.setStatus(400, "Bad Request");
                res.setBody("No multipart data found");
                return;
            }

            StringBuilder result = new StringBuilder();
            for (MultipartPart part : parts) {
                if (part.getFilename() != null) {
                    // файл
                    result.append("File: ")
                          .append(part.getFilename())
                          .append(", size: ")
                          .append(part.getBytes().length)
                          .append(" bytes\n");
                } else {
                    // текстовое поле
                    result.append("Field: ")
                          .append(part.getName())
                          .append(" = ")
                          .append(part.getValue())
                          .append("\n");
                }
            }

            res.setStatus(200, "OK");
            res.setBody(result.toString());
        });

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}