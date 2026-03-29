package org.example.http;

import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String httpVersion;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> queryParams;

    public HttpRequest(HttpMethod method, String path, String httpVersion, Map<String, String> headers, String body, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
}