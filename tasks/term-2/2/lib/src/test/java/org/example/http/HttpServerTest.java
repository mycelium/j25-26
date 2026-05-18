package org.example.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private HttpServer server;
    private static final int PORT = 18080;

    @BeforeEach
    void setUp() throws InterruptedException {
        server = HttpServer.builder()
                .port(PORT)
                .threads(2)
                .isVirtual(false)
                .route("/hello", HttpMethod.GET, req -> new HttpResponse().text("Hello, World!"))
                .route("/echo", HttpMethod.POST, req -> new HttpResponse().text(req.getBody()))
                .route("/json", HttpMethod.GET, req -> new HttpResponse().json("{\"status\":\"ok\"}"))
                .build();
        server.start();
        Thread.sleep(200);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetRequest() throws IOException {
        String response = sendRequest("GET /hello HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("Hello, World!"));
    }

    @Test
    void testPostRequest() throws IOException {
        String body = "test body";
        String response = sendRequest(
                "POST /echo HTTP/1.1\r\nHost: localhost\r\nContent-Length: " + body.length() + "\r\nConnection: close\r\n\r\n" + body
        );
        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("test body"));
    }

    @Test
    void testNotFound() throws IOException {
        String response = sendRequest("GET /notexist HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
        assertTrue(response.contains("404"));
    }

    @Test
    void testJsonResponse() throws IOException {
        String response = sendRequest("GET /json HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n");
        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("application/json"));
        assertTrue(response.contains("{\"status\":\"ok\"}"));
    }

    private String sendRequest(String request) throws IOException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
                sb.append(new String(buf, 0, read, StandardCharsets.UTF_8));
            }
            return sb.toString();
        }
    }
}
