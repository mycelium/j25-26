package httpserver;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String statusMessage;
    private final Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        this.statusCode = 200;
        this.statusMessage = "OK";
        this.headers = new HashMap<>();
        this.body = new byte[0];
        headers.put("Content-Type", "text/plain; charset=UTF-8");
    }

    public HttpResponse setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
        return this;
    }

    public HttpResponse setStatus(int code) {
        this.statusCode = code;
        this.statusMessage = defaultMessage(code);
        return this;
    }

    public HttpResponse setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpResponse setBody(String text) {
        this.body = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public HttpResponse setBody(byte[] bytes) {
        this.body = bytes.clone();
        headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");

        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(body.length));
        }
        headers.put("Connection", "close");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(body, 0, result, headerBytes.length, body.length);
        return result;
    }

    private static String defaultMessage(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
