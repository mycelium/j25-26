package ru.task2.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int status = 200;
    private String body = "";
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse status(int status) {
        this.status = status;
        return this;
    }

    public HttpResponse body(String body) {
        this.body = body;
        return this;
    }

    public HttpResponse header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public byte[] toBytes() {

        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ")
                .append(status)
                .append(" ")
                .append(getStatusText())
                .append("\r\n");

        headers.putIfAbsent("Content-Length",
                String.valueOf(body.getBytes().length));

        headers.putIfAbsent("Content-Type",
                "text/plain");

        for (var entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }

        sb.append("\r\n");
        sb.append(body);

        return sb.toString().getBytes();
    }

    private String getStatusText() {
        return switch (status) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}