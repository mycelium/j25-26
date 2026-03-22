package http;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main {
    
    private static final String HOST = "localhost";
    private static final int PORT = 8082; 
    private static final int THREAD_POOL_SIZE = 4;
    private static final boolean IS_VIRTUAL = true;
    
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer(HOST, PORT, IS_VIRTUAL, THREAD_POOL_SIZE);
        registerRoutes(server);
        Thread serverThread = new Thread(server::start, "Server-Main");
        serverThread.start();
        runAllTests();
        System.out.println("\nTests finished. Server is still running. Use curl now in new terminal.");
        System.out.println("Press ENTER to stop server...");
        System.in.read();
        server.stop();
    }
    
    private static void registerRoutes(HttpServer server) {
        //GET 
        server.addRoutes("GET", "/hello", req -> 
            new HttpResponse(200, "", "GET: Hello World")
        );
        
        server.addRoutes("GET", "/threadinfo", req -> {
        String body = " ";
        return new HttpResponse().setStatusCode(200).setBody(body);
        });
        
        server.addRoutes("GET", "/headers", req -> {
            StringBuilder sb = new StringBuilder("Headers received:\n");
            req.getHeaders().forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            return new HttpResponse(200, "", sb.toString());
        });
        
        //POST 
        server.addRoutes("POST", "/data", req -> 
            new HttpResponse(201, "", "POST received: " + req.getBody())
        );
        
        server.addRoutes("POST", "/json", req -> {
            String contentType = req.getHeaders().get("Content-Type");
            return new HttpResponse(201, "", 
                "JSON endpoint\nContent-Type: " + contentType + "\nBody: " + req.getBody());
        });

        server.addRoutes("POST", "/multipart", req -> {
            Map<String, String> parts = req.getFormFields();
            Map<String, byte[]> files = req.getFileParts();
            if (parts.isEmpty() && files.isEmpty()) {
                return new HttpResponse(400, "", "Multipart parsing failed or no fields/files");
            }
            StringBuilder sb = new StringBuilder("Parsed multipart data:\n");
            parts.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
            files.forEach((k, v) -> sb.append(k).append(" (file) size=").append(v.length).append(" bytes\n"));
            return new HttpResponse(200, "", sb.toString());
        });
        
        //PUT 
        server.addRoutes("PUT", "/update", req -> 
            new HttpResponse(200, "", "PUT update: " + req.getBody())
        );
        
        //PATCH
        server.addRoutes("PATCH", "/partial", req -> 
            new HttpResponse(200, "", "PATCH applied: " + req.getBody())
        );
        
        //DELETE
        server.addRoutes("DELETE", "/remove", req -> 
            new HttpResponse(200, "", "DELETE successful - resource removed")
        );
        
        server.addRoutes("GET", "/unknown", req -> 
            new HttpResponse(404, "", "Path not implemented")
        );
    }
    
    private static void runAllTests() throws Exception {
        System.out.println(" Methods tests");
        System.out.println("-".repeat(50));
        performRawRequest("GET", "/hello", null);
        performRawRequest("GET", "/headers", null);
        performRawRequest("POST", "/data", "Test POST body");
        performRawRequest("PUT", "/update", "PUT body data");
        performRawRequest("PATCH", "/partial", "PATCH body data");
        performRawRequest("DELETE", "/remove", null);
        performMultipartRequest();
        System.out.println("\nConcurrent Requests (Multi-threading Demo)");
        System.out.println("-".repeat(50));
        testConcurrency(IS_VIRTUAL);
        if (IS_VIRTUAL) {
            System.out.println("\nTesting fixed thread pool for comparison:");
            System.out.println("-".repeat(50));
            testConcurrency(false);
        } else {
            System.out.println("\nTesting virtual threads for comparison:");
            System.out.println("-".repeat(50));
            testConcurrency(true);
        }   
    }

    private static void performRawRequest(String method, String path, String body) {
        try {
            StringBuilder raw = new StringBuilder();
            raw.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
            raw.append("Host: ").append(HOST).append(":").append(PORT).append("\r\n");
            raw.append("Connection: close\r\n");

            if (body != null && !body.isEmpty()) {
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                raw.append("Content-Type: text/plain; charset=utf-8\r\n");
                raw.append("Content-Length: ").append(bodyBytes.length).append("\r\n\r\n");
                raw.append(body);
            } else {
                raw.append("Content-Length: 0\r\n\r\n");
            }

            String response = httpRequest(raw.toString());
            System.out.println("Request to " + path + ":\n" + response + "\n");
            if (response.contains("HTTP/1.1 200 OK") || response.contains("HTTP/1.1 201 Created")) {
                System.out.println(method + " test passed\n\n");
            } else {
                System.out.println(method + " test failed\n\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void performMultipartRequest() {
        try {
            String boundary = "bbbbbbbbbbbbbBoundary123456789"; 
            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"field1\"\r\n\r\n");
            body.append("value1\r\n");
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"file1\"; filename=\"a.txt\"\r\n");
            body.append("Content-Type: text/plain\r\n\r\n");
            body.append("Hello file\r\n");
            body.append("--").append(boundary).append("--\r\n");

            byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);

            StringBuilder raw = new StringBuilder();
            raw.append("POST /multipart HTTP/1.1\r\n");
            raw.append("Host: ").append(HOST).append(":").append(PORT).append("\r\n");
            raw.append("Connection: close\r\n");
            raw.append("Content-Type: multipart/form-data; boundary=").append(boundary).append("\r\n");
            raw.append("Content-Length: ").append(bodyBytes.length).append("\r\n\r\n");
            raw.append(body);

            String response = httpRequest(raw.toString());
            System.out.println("Request to /multipart:\n" + response + "\n");
            System.out.println(response.contains("HTTP/1.1 200 OK") ? "multipart test passed\n\n" : "multipart test failed\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     
    private static void testConcurrency(boolean isVirtual) throws Exception {
        ExecutorService executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int number_latch = 6;
        CountDownLatch latch = new CountDownLatch(number_latch);
        long start = System.currentTimeMillis(); 
        
        System.out.println("Using " + (isVirtual ? "virtual threads" : "fixed thread pool") + "\n");

        IntStream.rangeClosed(1, number_latch).forEach(i -> {
            executor.submit(() -> {
                try {
                    String threadInfo = Thread.currentThread().isVirtual()
                            ? "VirtualThread-" + Thread.currentThread().threadId()
                            : Thread.currentThread().getName();
                    System.out.println("  [" + threadInfo + "] Request #" + i + " started");
                    StringBuilder raw = new StringBuilder();
                    raw.append("GET /threadinfo HTTP/1.1\r\n");
                    raw.append("Host: ").append(HOST).append(":").append(PORT).append("\r\n");
                    raw.append("Connection: close\r\n");
                    raw.append("Content-Length: 0\r\n\r\n");
                    String responseBody = httpRequest(raw.toString());
                    System.out.println("  [" + threadInfo + "] Request #" + i + " done ");
                    
                } catch (Exception e) {
                    System.err.println("  Error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        });
        
        latch.await(10, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - start;
        
        System.out.println("\nTotal time: " + duration + "ms");
        executor.shutdown();
    }
    
    private static String httpRequest(String rawRequest) throws IOException {
        try (Socket socket = new Socket(HOST, PORT)) {
            OutputStream out = socket.getOutputStream();
            out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[1024];
            int read;
            while ((read = in.read(temp)) != -1) {
                buffer.write(temp, 0, read);
            }

            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}