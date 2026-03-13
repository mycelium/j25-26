package http;

import http.HttpServer;
import http.HttpResponse;

public class Main {
    public static void main(String[] args) {
        // Создаем сервер
        HttpServer server = new HttpServer("localhost", 8081, 10, true);

        // 1. Обработка GET запроса
        server.addHandler("GET", "/hello", request -> {
            return new HttpResponse(200, "OK", "Hello from GET!");
        });

        // 2. Обработка POST запроса (обычный текст)
        server.addHandler("POST", "/data", request -> {
            String body = request.getBody();
            System.out.println("Received body: " + body);
            return new HttpResponse(201, "Created", "Data received: " + body);
        });

        // 3. Обработка POST запроса (БОНУС: multipart/form-data)
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

        // 4. Обработка DELETE запроса
        server.addHandler("DELETE", "/delete", request -> {
            return new HttpResponse(200, "OK", "Item deleted");
        });

        // Запускаем сервер
        server.start();
    }
}