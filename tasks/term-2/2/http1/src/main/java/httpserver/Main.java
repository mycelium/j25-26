package httpserver;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create("localhost", 8080, 4, true)

                .get("/hello", (req, res) ->
                        res.header("content-type", "text/plain; charset=utf-8")
                                .body("Hello from Gradle!"))

                .post("/echo", (req, res) ->
                        res.header("content-type", "text/plain")
                                .body(req.bodyAsString()))

                .post("/upload", (req, res) -> {
                    String contentType = req.header("content-type");
                    var result = Multipart.parse(req.body(), contentType);

                    if (!result.files().isEmpty()) {
                        String fileName = result.files().keySet().iterator().next();
                        int size = result.files().get(fileName).length;
                        res.body("Received file: " + fileName + " (" + size + " bytes)");
                    } else {
                        res.status(400).body("No file part found");
                    }
                })
                .delete("/item/{id}", (req, res) ->
                        res.status(200).body("Deleted resource: " + req.path()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                System.out.println("Server stopped.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        server.start();
    }
}