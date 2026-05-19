package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Response {
    private int status = 200;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public int status() { return status; }

    public Response status(int code) {
        this.status = code;
        return this;
    }

    public Response header(String name, String value) {
        headers.put(name.toLowerCase(), value);
        return this;
    }

    public Response body(String text) {
        return body(text.getBytes(StandardCharsets.UTF_8));
    }

    public Response body(byte[] data) {
        this.body = data.clone();
        headers.put("content-length", String.valueOf(body.length));
        return this;
    }
    private String reason(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    public byte[] toBytes() {
        headers.putIfAbsent("connection", "close");
        headers.putIfAbsent("content-length", String.valueOf(body.length));
        if (!headers.containsKey("connection")) {
            headers.put("connection", "close");
        }
        if (!headers.containsKey("content-length")) {
            headers.put("content-length", String.valueOf(body.length));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(' ')
                .append(reason(status)).append("\r\n");

        for (Map.Entry<String, String> h : headers.entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] head = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[head.length + body.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(body, 0, result, head.length, body.length);
        return result;
    }
}