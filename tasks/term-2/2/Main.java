import httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("localhost", 8080, 4, false);

        server.addRoute(HttpMethod.GET, "/", req -> {
            HttpResponse resp = new HttpResponse();
            resp.setBody("Hello World!", "text/plain; charset=utf-8");
            return resp;
        });

        server.addRoute(HttpMethod.POST, "/echo", req -> {
            HttpResponse resp = new HttpResponse();
            resp.setBody("Echo: " + req.getBodyAsString(), "text/plain; charset=utf-8");
            return resp;
        });

        server.addRoute(HttpMethod.PUT, "/update", req -> {
            HttpResponse resp = new HttpResponse(201, "Updated");
            return resp;
        });

        server.addRoute(HttpMethod.PATCH, "/patch", req -> {
            HttpResponse resp = new HttpResponse(200, "Patched");
            return resp;
        });

        server.addRoute(HttpMethod.DELETE, "/delete", req -> {
            HttpResponse resp = new HttpResponse(204, "");
            return resp;
        });

        server.start();
        System.out.println("Server running at http://localhost:8080");
        System.out.println("Press Enter to stop...");
        System.in.read();
        server.stop();
    }
}