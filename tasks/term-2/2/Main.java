import http.Engine;
import http.ReqMethod;

public class Main {
    public static void main(String[] args) throws Exception {
        Engine engine = new Engine("127.0.0.1", 12345, 4, false);

        // GET запрос
        engine.route(ReqMethod.GET, "/api/status", (req, res) -> {
            res.setPayload("Server is running!");
        });
        
        // POST запрос
        engine.route(ReqMethod.POST, "/api/mirror", (req, res) -> {
            String body = req.getPayloadAsString();
            res.setPayload("You sent: " + body);
        });

        // POST запрос
        engine.route(ReqMethod.POST, "/api/upload", (req, res) -> {
            String user = req.getFormData().getOrDefault("username", "Guest");
            res.setPayload("Welcome to the system, " + user + "!");
        });

        engine.launch();
    }
}