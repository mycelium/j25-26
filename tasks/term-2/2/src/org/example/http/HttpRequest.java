package org.example.http;

import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> queryParams; // Бонус: параметры из URL ?key=val

    public HttpRequest(HttpMethod method, String path, String protocol, 
                       Map<String, String> headers, byte[] body, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams;
    }

    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public String getProtocol() { return protocol; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body); }
    public Map<String, String> getQueryParams() { return queryParams; }
    
    // Helper to get specific header case-insensitively
    public String getHeader(String name) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}