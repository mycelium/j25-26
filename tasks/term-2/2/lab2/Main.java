package lab2;

import lab2.http.ServerResponse;
import lab2.http.WebEngine;

public class Main {
    public static void main(String[] args) {
        WebEngine app = new WebEngine("127.0.0.1", 8082, 15, true);

        // 1. GET
        app.registerRoute("GET", "/api/ping", request -> {
            return new ServerResponse(200, "OK", "{\"status\": \"Server is alive\"}");
        });

        // 2. POST (Обычный)
        app.registerRoute("POST", "/api/store", request -> {
            System.out.println("POST Payload: " + request.getTextBody());
            return new ServerResponse(201, "Created", "{\"result\": \"Successfully stored\"}");
        });

        // 3. PUT (Полное обновление)
        app.registerRoute("PUT", "/api/update", request -> {
            System.out.println("PUT Payload: " + request.getTextBody());
            return new ServerResponse(200, "OK", "{\"result\": \"Resource updated\"}");
        });

        // 4. PATCH (Частичное обновление)
        app.registerRoute("PATCH", "/api/modify", request -> {
            System.out.println("PATCH Payload: " + request.getTextBody());
            return new ServerResponse(200, "OK", "{\"result\": \"Resource patched\"}");
        });

        // 5. DELETE
        app.registerRoute("DELETE", "/api/remove", request -> {
            return new ServerResponse(200, "OK", "{\"result\": \"Resource wiped\"}");
        });

        // БОНУС: POST Multipart
        app.registerRoute("POST", "/api/upload", request -> {
            System.out.println("Extracted fields: " + request.getParsedMultipart());
            String title = request.getParsedMultipart().getOrDefault("title", "No Title");
            return new ServerResponse(200, "OK", "{\"savedTitle\": \"" + title + "\"}");
        });

        app.launch();
    }
}