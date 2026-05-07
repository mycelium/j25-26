package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {
    private int statusCode = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public HttpResponse(int statusCode, byte[] body, String contentType) {
        this.statusCode = statusCode;
        this.body = body;
        headers.put("Content-Type", contentType);
    }

    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public int getStatusCode() { return statusCode; }

    public void setBody(String body) { this.body = body.getBytes(StandardCharsets.UTF_8); }
    public void setBody(String body, String contentType) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", contentType);
    }
    public void setBody(byte[] body, String contentType) {
        this.body = body;
        headers.put("Content-Type", contentType);
    }
    public byte[] getBody() { return body; }

    public void addHeader(String name, String value) { headers.put(name, value); }
    public Map<String, String> getHeaders() { return headers; }
}