import httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("127.0.0.1", 8080, 4, false);

        server.addHandler("/hello", HttpMethod.GET, (req, res) -> {
            String name = req.getParam("name");
            res.setHtml("<h1>Hello, " + (name != null ? name : "Guest") + "</h1>");
        });

        server.addHandler("/echo", HttpMethod.POST, (req, res) -> {
            res.setBody("You sent: " + req.getBodyAsString());
        });

        server.addHandler("/upload", HttpMethod.POST, (req, res) -> {
            if (req.isMultipart()) {
                res.setBody("Received parts: " + req.getParts().size());
            } else {
                res.setStatus(400).setBody("Not a multipart request");
            }
        });

        System.out.println("Server started at http://127.0.0.1:8080");
        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(30000);
        System.out.println("server stop...");
        server.stop();
    }
}