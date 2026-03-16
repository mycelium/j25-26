package com.httpserver;

import com.httpserver.core.NioHttpServer;
import com.httpserver.api.HttpMethod;
import com.httpserver.api.HttpRequest;
import com.httpserver.api.HttpResponse;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        
        // 1. Создание сервера с конфигурацией host, port, threadCount и isVirtual (Требование выполнено)
        NioHttpServer server = NioHttpServer.builder()
                .host("127.0.0.1")
                .port(8080)
                .threadCount(8)       // Number of thread should be configurable
                .isVirtual(true)      // Add boolean parameter: isVirtual (требует Java 21+)
                .build();

        // 2. GET-запрос: Демонстрация доступа к заголовкам и query-параметрам
        server.addListener(HttpMethod.GET, "/api/info", request -> {
            // Headers are accessible as Map
            String userAgent = request.getHeaders().getOrDefault("user-agent", "Unknown");
            String userId = request.getQueryParams().getOrDefault("id", "Not provided");

            String responseText = String.format("GET Method.\nUser-Agent: %s\nRequested User ID: %s", userAgent, userId);

            // Process HttpResponse
            return HttpResponse.builder()
                    .statusCode(200)
                    .header("Content-Type", "text/plain")
                    .body(responseText)
                    .build();
        });

        // 3. POST-запрос: Демонстрация чтения Body (тела запроса)
        server.addListener(HttpMethod.POST, "/api/data", request -> {
            String requestBody = request.getBodyAsString();
            
            return HttpResponse.builder()
                    .statusCode(201) // Created
                    .body("POST Method. Received body: " + requestBody)
                    .build();
        });

        // 4. PUT-запрос
        server.addListener(HttpMethod.PUT, "/api/data", request -> {
            String requestBody = request.getBodyAsString();
            
            return HttpResponse.builder()
                    .statusCode(200)
                    .body("PUT Method. Resource fully updated with: " + requestBody)
                    .build();
        });

        // 5. PATCH-запрос
        server.addListener(HttpMethod.PATCH, "/api/data", request -> {
            String requestBody = request.getBodyAsString();
            
            return HttpResponse.builder()
                    .statusCode(200)
                    .body("PATCH Method. Resource partially updated with: " + requestBody)
                    .build();
        });

        // 6. DELETE-запрос
        server.addListener(HttpMethod.DELETE, "/api/data", request -> {
            return HttpResponse.builder()
                    .statusCode(204) // No Content - стандартный код для успешного удаления без тела ответа
                    .build();
        });

        // 7. BONUS: Multipart form data (POST запрос)
        server.addListener(HttpMethod.POST, "/api/upload", request -> {
            StringBuilder sb = new StringBuilder("MULTIPART BONUS:\n");

            if (request.getParts() != null && !request.getParts().isEmpty()) {
                int partNumber = 1;
                for (HttpRequest.MultiPart part : request.getParts()) {
                    sb.append("Part #").append(partNumber++).append("\n");
                    // Доступ к заголовкам конкретной части (например, Content-Disposition)
                    sb.append("  Headers: ").append(part.getHeaders()).append("\n");
                    sb.append("  Content: ").append(part.getContentAsString()).append("\n\n");
                }
            } else {
                sb.append("No multipart data found or invalid format.");
            }

            return HttpResponse.builder()
                    .statusCode(200)
                    .body(sb.toString())
                    .build();
        });

        // Запуск сервера
        server.start();
        
        // Чтобы программа не завершилась сразу после запуска потоков
        System.out.println("Main thread is sleeping. Press Ctrl+C to stop the server.");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}