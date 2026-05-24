package lab2;

import lab2.http.HttpResponse;
import lab2.http.HttpServer;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer("127.0.0.1", 8082, 15, true);

        server.registerRoute("GET", "/api/ping", request ->
                new HttpResponse(200, "{\"status\": \"Server is alive\"}"));

        server.registerRoute("POST", "/api/store", request -> {
            System.out.println("POST body: " + request.getBody());
            return new HttpResponse(201, "{\"result\": \"Successfully stored\"}");
        });

        server.registerRoute("PUT", "/api/update", request -> {
            System.out.println("PUT body: " + request.getBody());
            return new HttpResponse(200, "{\"result\": \"Resource updated\"}");
        });

        server.registerRoute("PATCH", "/api/modify", request -> {
            System.out.println("PATCH body: " + request.getBody());
            return new HttpResponse(200, "{\"result\": \"Resource patched\"}");
        });

        server.registerRoute("DELETE", "/api/remove", request ->
                new HttpResponse(200, "{\"result\": \"Resource removed\"}"));

        server.registerRoute("POST", "/api/upload", request -> {
            System.out.println("Multipart fields: " + request.getMultipartFields());
            String title = request.getMultipartFields().getOrDefault("title", "No Title");
            return new HttpResponse(200, "{\"savedTitle\": \"" + title + "\"}");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "http-server-shutdown"));
        server.start();
    }
}
