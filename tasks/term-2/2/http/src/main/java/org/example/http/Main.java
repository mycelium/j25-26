package org.example.http;

import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer("localhost", 9090, 4, false);

        server.addHandler(HttpMethod.GET, "/hello", request ->
                HttpResponse.ok("Hello from server!"));

        server.addHandler(HttpMethod.POST, "/echo", request ->
                HttpResponse.ok("Body: " + request.getBody()));

        server.addHandler(HttpMethod.GET, "/greet", request -> {
            String name = request.getQueryParam("name");
            return name != null
                    ? HttpResponse.ok("Hello, " + name + "!")
                    : HttpResponse.badRequest("Missing query parameter: name");
        });

        server.addHandler(HttpMethod.PUT, "/update", request ->
                HttpResponse.ok("Updated with: " + request.getBody()));

        server.addHandler(HttpMethod.PATCH, "/patch", request ->
                HttpResponse.ok("Patched with: " + request.getBody()));

        server.addHandler(HttpMethod.DELETE, "/delete", request ->
                HttpResponse.ok("Deleted resource"));

        server.start();

        System.out.println("=================================================");
        System.out.println("  HTTP server running");
        System.out.println("  Host:            " + server.getHost());
        System.out.println("  Port:            " + server.getPort());
        System.out.println("  Threads:         " + server.getThreadCount());
        System.out.println("  Virtual threads: " + server.isVirtual());
        System.out.println("=================================================");
        System.out.println("  curl.exe http://localhost:9090/hello");
        System.out.println("  curl.exe \"http://localhost:9090/greet?name=World\"");
        System.out.println("  curl.exe -X POST -d \"hello\" http://localhost:9090/echo");
        System.out.println("  curl.exe -X PATCH -d \"patched data\" http://localhost:9090/patch");
        System.out.println("  curl.exe -X DELETE http://localhost:9090/delete");
        System.out.println("=================================================");
        System.out.println("  Press Ctrl+C to stop");
        System.out.println("=================================================");

        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }, "shutdown-hook"));

        latch.await();
    }
}