package ru.task2.http;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @Test
    void testResponse() {

        HttpResponse response =
                new HttpResponse()
                        .status(200)
                        .body("Hello");

        String raw =
                new String(response.toBytes());

        assertTrue(raw.contains("Hello"));
        assertTrue(raw.contains("200"));
    }

    @Test
    void testHeaders() {

        HttpResponse response =
                new HttpResponse()
                        .header("X-Test", "123");

        String raw =
                new String(response.toBytes());

        assertTrue(raw.contains("X-Test"));
    }

    @Test
    void testStatusCode() {

        HttpResponse response =
                new HttpResponse()
                        .status(404);

        String raw =
                new String(response.toBytes());

        assertTrue(raw.contains("404"));
        assertTrue(raw.contains("Not Found"));
    }

    @Test
    void testRequestFields() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "localhost");

        HttpRequest request =
                new HttpRequest(
                        HttpMethod.GET,
                        "/hello",
                        headers,
                        "body"
                );

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/hello", request.getPath());
        assertEquals("localhost", request.getHeaders().get("Host"));
        assertEquals("body", request.getBody());
    }

    @Test
    void testRouteKeyEquals() {

        RouteKey r1 =
                new RouteKey(HttpMethod.GET, "/test");

        RouteKey r2 =
                new RouteKey(HttpMethod.GET, "/test");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
