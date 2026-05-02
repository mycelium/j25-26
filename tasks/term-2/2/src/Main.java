import com.httpserver.HttpServer;
import com.httpserver.HttpResponse;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 55555;
        
        HttpServer server = HttpServer.create("localhost", port);

        server.get("/", req -> {
            return HttpResponse.ok("Hello World!");
        });

        server.get("/hello", req -> {
            String name = req.getQueryParam("name");
            if (name == null) name = "Anonymous";
            return HttpResponse.ok("Hello, " + name + "!");
        });

        server.post("/echo", req -> {
            String body = req.getBodyAsString();
            return HttpResponse.ok("Echo: " + body);
        });

        System.out.println("Server started on http://localhost:" + port);
        server.start(10, false);
        
        System.out.println("Press Enter to stop...");
        System.in.read();
        
        server.stop();
    }
}