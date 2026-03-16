package com.httpserver.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final List<MultiPart> parts;

    public HttpRequest(HttpMethod method, String path, Map<String, String> queryParams,
                       Map<String, String> headers, byte[] body, List<MultiPart> parts) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
        this.parts = parts;
    }

    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return body != null ? new String(body, StandardCharsets.UTF_8) : ""; }
    public List<MultiPart> getParts() { return parts; }

    public static class MultiPart {
        private final Map<String, String> headers;
        private final byte[] content;

        public MultiPart(Map<String, String> headers, byte[] content) {
            this.headers = headers;
            this.content = content;
        }

        public Map<String, String> getHeaders() { return headers; }
        public String getHeader(String name) { return headers.get(name.toLowerCase()); }
        public byte[] getContent() { return content; }
        public String getContentAsString() { return new String(content, StandardCharsets.UTF_8); }
    }
}