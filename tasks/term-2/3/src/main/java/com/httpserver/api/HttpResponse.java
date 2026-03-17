package com.httpserver.api;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public int getStatusCode() { return statusCode; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int statusCode = 200;
        private final Map<String, String> headers = new HashMap<>();
        private byte[] body = new byte[0];

        public Builder statusCode(int statusCode) { this.statusCode = statusCode; return this; }
        public Builder header(String key, String value) { this.headers.put(key, value); return this; }
        public Builder body(String body) { this.body = body.getBytes(StandardCharsets.UTF_8); return this; }
        public Builder body(byte[] body) { this.body = body; return this; }
        
        public HttpResponse build() { return new HttpResponse(this); }
    }
}