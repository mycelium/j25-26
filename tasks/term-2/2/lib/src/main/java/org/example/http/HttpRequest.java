package org.example.http;

import java.util.Collections;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String body;

    HttpRequest(HttpMethod method, String path, Map<String, String> headers,
                Map<String, String> queryParams, String body) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.body = body;
    }

    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getBody() { return body; }
    public String getHeader(String name) { return headers.get(name.toLowerCase()); }
}
