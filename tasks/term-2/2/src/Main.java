import com.httpserver.HttpServer;
import com.httpserver.HttpResponse;
import com.httpserver.MultipartParser;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        int port = 55555;

        HttpServer server = HttpServer.create("127.0.0.1", port);

        // -----------------------------------------------------------------
        // GET /
        // -----------------------------------------------------------------

        server.get("/", req ->
            HttpResponse.ok("Hello World!")
        );

        // -----------------------------------------------------------------
        // GET /hello?name=John
        // -----------------------------------------------------------------

        server.get("/hello", req -> {

            String name = req
                    .getFirstQueryParam("name")
                    .orElse("Anonymous");

            return HttpResponse.ok("Hello, " + name + "!");
        });

        // -----------------------------------------------------------------
        // POST /echo
        // -----------------------------------------------------------------

        server.post("/echo", req -> {

            String body = req.getBodyAsString();

            return HttpResponse.ok("Echo: " + body);
        });

        // -----------------------------------------------------------------
        // POST /upload (multipart/form-data)
        // -----------------------------------------------------------------

        server.post("/upload", req -> {

            var boundaryOpt = MultipartParser.extractBoundary(req);

            if (boundaryOpt.isEmpty()) {
                return HttpResponse.badRequest(
                        "Content-Type must be multipart/form-data");
            }

            try {
                List<MultipartParser.Part> parts = MultipartParser.parse(
                        req.getBody(), 
                        boundaryOpt.get()
                );
                
                StringBuilder sb = new StringBuilder();
                sb.append("Upload successful!\n");
                sb.append("Number of parts: ").append(parts.size()).append("\n\n");

                for (MultipartParser.Part part : parts) {
                    sb.append("Part: ")
                      .append(part.getName().orElse("unknown"));

                    part.getFilename().ifPresent(filename ->
                        sb.append(" (file='").append(filename).append("')")
                    );

                    sb.append(" size=")
                      .append(part.getData().length)
                      .append(" bytes\n");
                }

                return HttpResponse.ok(sb.toString());
                
            } catch (IOException e) {
                // ✅ Retourner une erreur propre au client
                return HttpResponse.badRequest(
                        "Failed to parse multipart data: " + e.getMessage()
                );
            }
        });

        // -----------------------------------------------------------------
        // START
        // -----------------------------------------------------------------

        System.out.println("Server started on http://localhost:" + port);
        System.out.println("\nTest commands:");
        System.out.println("  curl http://localhost:" + port + "/");
        System.out.println("  curl 'http://localhost:" + port + "/hello?name=John'");
        System.out.println("  curl -X POST http://localhost:" + port + "/echo -d 'Hello'");
        System.out.println("  curl -F 'file=@test.txt' http://localhost:" + port + "/upload");
        System.out.println("\nPress Enter to stop...");
        
        // threadCount = 10
        // useVirtualThreads = true
        server.start(10, true);

        System.in.read();
        server.stop();
    } 
}