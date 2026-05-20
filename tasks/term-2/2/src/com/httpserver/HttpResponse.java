package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class HttpResponse {

    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(int statusCode, String statusMessage,
                         Map<String, String> headers, byte[] body) {
        this.statusCode    = statusCode;
        this.statusMessage = statusMessage;
        this.headers       = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body          = body == null ? new byte[0] : body.clone();
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    public static HttpResponse ok(String body) {
        return text(200, "OK", body);
    }

    public static HttpResponse ok(byte[] body, String contentType) {
        return bytes(200, "OK", body, contentType);
    }

    public static HttpResponse created(String body) {
        return text(201, "Created", body);
    }

    public static HttpResponse badRequest(String body) {
        return text(400, "Bad Request", body);
    }

    public static HttpResponse notFound(String body) {
        return text(404, "Not Found", body);
    }

    public static HttpResponse methodNotAllowed(String allowedMethods) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Allow", allowedMethods);
        byte[] bytes = "Method Not Allowed".getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "text/plain; charset=utf-8");
        headers.put("Content-Length", String.valueOf(bytes.length));
        return new HttpResponse(405, "Method Not Allowed", headers, bytes);
    }

    public static HttpResponse internalServerError(String body) {
        return text(500, "Internal Server Error", body);
    }

    // -------------------------------------------------------------------------
    // Helpers internes
    // -------------------------------------------------------------------------

    private static HttpResponse text(int code, String message, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=utf-8");
        headers.put("Content-Length", String.valueOf(bytes.length));
        return new HttpResponse(code, message, headers, bytes);
    }

    private static HttpResponse bytes(int code, String message, byte[] body, String contentType) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Content-Length", String.valueOf(body.length));
        return new HttpResponse(code, message, headers, body);
    }

    // -------------------------------------------------------------------------
    // Sérialisation
    // -------------------------------------------------------------------------

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusMessage).append("\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("\r\n");

        byte[] head = sb.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[head.length + body.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(body, 0, result, head.length, body.length);
        return result;
    }

    public int getStatusCode() { return statusCode; }
}