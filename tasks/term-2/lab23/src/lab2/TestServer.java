package lab2;
import java.io.IOException;

public class TestServer {
    
    public static void main(String[] args) {
        HttpServer server = new HttpServer("localhost", 8080);

        server.setThreadPoolSize(5);
        server.setVirtualThreads(false);

        server.get("/", (req, res) -> {
            res.setStatus(200);
            res.setHeader("Content-Type", "text/html");
            res.setBody("<h1>test HTTP</h1>");
        });

        server.get("/api/users", (req, res) -> {
            res.setStatus(200);
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"some\":\"info\"}");
        });

        server.post("/api/users", (req, res) -> {
            String body = req.getBody();
            System.out.println("получен запрос: " + body);
            
            res.setStatus(201);
            res.setHeader("Content-Type", "application/json");
            res.setBody("{\"received\": " + body + "}");
        });
        
        server.put("/api/users/update", (req, res) -> {
            res.setStatus(200);
            res.setBody("{\"status\": \"updated\"}");
        });

        server.delete("/api/users/delete", (req, res) -> {
            res.setStatus(204);
            res.setBody("");
        });

        try {
            server.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}