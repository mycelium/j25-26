package com.webserver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final byte[] bodyBytes;

    public Request(String method, String path, Map<String, String> headers, byte[] bodyBytes) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.bodyBytes = bodyBytes;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    public byte[] getRawBody() {
        return bodyBytes;
    }
}
