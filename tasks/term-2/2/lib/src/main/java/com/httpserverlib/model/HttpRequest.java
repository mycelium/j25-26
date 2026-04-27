package com.httpserverlib.model;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String queryString;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final List<Part> multipartParts;

    HttpRequest(HttpMethod method, String path, String queryString,
                Map<String, String> queryParams, Map<String, String> headers,
                byte[] body, List<Part> multipartParts) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;

        // Create case‑insensitive mutable maps first, then make unmodifiable
        Map<String, String> tmpQueryParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        tmpQueryParams.putAll(queryParams);
        this.queryParams = Collections.unmodifiableMap(tmpQueryParams);

        Map<String, String> tmpHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        tmpHeaders.putAll(headers);
        this.headers = Collections.unmodifiableMap(tmpHeaders);

        this.body = body != null ? body.clone() : new byte[0];
        this.multipartParts = multipartParts != null ? List.copyOf(multipartParts) : List.of();
    }

    // Getters (same as before)
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public String getQueryString() { return queryString; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getQueryParam(String name) { return queryParams.get(name); }
    public Map<String, String> getHeaders() { return headers; }
    public String getHeader(String name) { return headers.get(name); }
    public byte[] getBody() { return body.clone(); }
    public String getBodyAsString() { return new String(body, StandardCharsets.UTF_8); }
    public List<Part> getMultipartParts() { return multipartParts; }
    public boolean isMultipart() { return !multipartParts.isEmpty(); }

    // Builder (unchanged, but included for completeness)
    public static class Builder {
        private HttpMethod method;
        private String path;
        private String queryString;
        private Map<String, String> queryParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        private byte[] body;
        private List<Part> multipartParts;

        public Builder method(HttpMethod method) { this.method = method; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder queryString(String queryString) { this.queryString = queryString; return this; }
        public Builder queryParam(String name, String value) { queryParams.put(name, value); return this; }
        public Builder queryParams(Map<String, String> params) { queryParams.putAll(params); return this; }
        public Builder header(String name, String value) { headers.put(name, value); return this; }
        public Builder headers(Map<String, String> headers) { this.headers.putAll(headers); return this; }
        public Builder body(byte[] body) { this.body = body; return this; }
        public Builder multipartParts(List<Part> parts) { this.multipartParts = parts; return this; }
        public HttpRequest build() {
            return new HttpRequest(method, path, queryString, queryParams, headers, body, multipartParts);
        }
    }
}