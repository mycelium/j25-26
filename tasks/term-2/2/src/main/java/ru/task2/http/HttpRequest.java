package ru.task2.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(
            HttpMethod method,
            String path,
            Map<String, String> headers,
            String body
    ) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}