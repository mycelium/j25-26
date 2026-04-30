package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(String method, String path, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }

    public String getBody() { return new String(body, StandardCharsets.UTF_8); }
    public byte[] getBodyBytes() { return body; }
}