import httpserverlib.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("localhost", 55555, 4, false);

        server.addListener("/hello", HttpMethod.GET, (req, res) -> {
            res.setBody("Hello, World!");
        });
        
        server.addListener("/echo", HttpMethod.POST, (req, res) -> {
            res.setBody("Echo: " + new String(req.body));
        });

        server.addListener("/form", HttpMethod.POST, (req, res) -> {
            String name = req.multipartFields.getOrDefault("name", "unknown");
            res.setBody("Hello, " + name);
        });

        server.start();
    }
}