package com.httpserver;

import java.util.*;

public class HttpResponse {
    public int statusCode;
    public String statusMessage;
    public final Map<String, String> headers = new HashMap<>();
    public byte[] body;
    
    public HttpResponse(int statusCode, String statusMessage) {
        this(statusCode, statusMessage, new byte[0]);
    }
    
    public HttpResponse(int statusCode, String statusMessage, byte[] body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }
    
    public HttpResponse withHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }
    
    public HttpResponse withJson() {
        headers.put("Content-Type", "application/json");
        return this;
    }
    
    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        headers.put("Content-Length", String.valueOf(body.length));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        
        byte[] headerBytes = sb.toString().getBytes();
        byte[] result = Arrays.copyOf(headerBytes, headerBytes.length + body.length);
        System.arraycopy(body, 0, result, headerBytes.length, body.length);
        return result;
    }
    
    public static HttpResponse ok(String body) {
        return new HttpResponse(200, "OK", body.getBytes());
    }
    
    public static HttpResponse ok(byte[] body) {
        return new HttpResponse(200, "OK", body);
    }
    
    public static HttpResponse notFound(String message) {
        return new HttpResponse(404, "Not Found", message.getBytes());
    }
    
    public static HttpResponse badRequest(String message) {
        return new HttpResponse(400, "Bad Request", message.getBytes());
    }
    
    public static HttpResponse internalError(String message) {
        return new HttpResponse(500, "Internal Server Error", message.getBytes());
    }
}