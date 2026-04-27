package com.httpserverlib.model;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    public static HttpMethod fromString(String method) {
        try { return HttpMethod.valueOf(method.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}