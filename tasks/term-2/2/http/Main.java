package http;

import http.HttpServer;
import http.HttpResponse;

public class Main {
    public static void main(String[] args) {

        HttpServer server = new HttpServer("localhost", 8081, 10, true);

        // 1. GET
        server.addHandler("GET", "/hello", request -> {
            return new HttpResponse(200, "OK", "Hello from GET!");
        });

        // 2. POST (Обычный текст)
        server.addHandler("POST", "/data", request -> {
            String body = request.getBody();
            System.out.println("Received body: " + body);
            return new HttpResponse(201, "Created", "Data received: " + body);
        });

        // 3. PUT (Полное обновление) - НОВЫЙ МЕТОД
        server.addHandler("PUT", "/update", request -> {
            String body = request.getBody();
            System.out.println("PUT update payload: " + body);
            return new HttpResponse(200, "OK", "Item completely updated with: " + body);
        });

        // 4. PATCH (Частичное обновление) - НОВЫЙ МЕТОД
        server.addHandler("PATCH", "/patch", request -> {
            String body = request.getBody();
            System.out.println("PATCH payload: " + body);
            return new HttpResponse(200, "OK", "Item partially patched with: " + body);
        });

        // 5. DELETE
        server.addHandler("DELETE", "/delete", request -> {
            return new HttpResponse(200, "OK", "Item deleted");
        });

        // 6. БОНУС: POST Multipart
        server.addHandler("POST", "/upload", request -> {
            System.out.println("Parsed Form Data: " + request.getFormData());
            String responseBody = "Received fields: ";
            if (request.getFormData().containsKey("username")) {
                responseBody += "Username = " + request.getFormData().get("username") + "; ";
            }
            if (request.getFormData().containsKey("document")) {
                responseBody += "Document text = " + request.getFormData().get("document");
            }
            return new HttpResponse(200, "OK", responseBody);
        });

        server.start();
    }
}