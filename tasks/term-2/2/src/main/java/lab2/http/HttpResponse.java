package lab2.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final String body;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(int statusCode, String body) {
        this(statusCode, defaultReasonPhrase(statusCode), body);
    }

    public HttpResponse(int statusCode, String reasonPhrase, String body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body == null ? "" : body;

        headers.put("Connection", "close");
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", String.valueOf(this.body.getBytes(StandardCharsets.UTF_8).length));
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ")
                .append(statusCode)
                .append(' ')
                .append(reasonPhrase)
                .append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        response.append("\r\n").append(body);
        return response.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String defaultReasonPhrase(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 411 -> "Length Required";
            case 413 -> "Payload Too Large";
            case 500 -> "Internal Server Error";
            default -> "Status";
        };
    }
}
