package com.httpserver;

import java.util.*;

public class HttpRequest {
    public final String method;
    public final String path;
    public final Map<String, String> headers = new HashMap<>();
    public final Map<String, List<String>> queryParams = new HashMap<>();
    public final Map<String, String> pathParams = new HashMap<>();
    public final byte[] body;
    
    public HttpRequest(String method, String path, byte[] body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }
    
    public String getBodyAsString() {
        return body != null ? new String(body) : "";
    }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
    
    public String getPathParam(String name) {
        return pathParams.get(name);
    }
}