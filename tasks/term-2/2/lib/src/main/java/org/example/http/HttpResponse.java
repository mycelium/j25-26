package org.example.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String body = "";

    public HttpResponse status(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
        return this;
    }

    public HttpResponse header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public HttpResponse body(String body) {
        this.body = body == null ? "" : body;
        return this;
    }

    public HttpResponse json(String json) {
        this.headers.put("Content-Type", "application/json; charset=UTF-8");
        this.body = json == null ? "" : json;
        return this;
    }

    public HttpResponse text(String text) {
        this.headers.put("Content-Type", "text/plain; charset=UTF-8");
        this.body = text == null ? "" : text;
        return this;
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
}
