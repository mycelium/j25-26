package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        headers.put("Content-Length", "0");
    }

    public HttpResponse status(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
        return this;
    }

    public HttpResponse header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public HttpResponse body(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public HttpResponse body(byte[] body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length));
        return this;
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusMessage).append("\r\n");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        byte[] head = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
        byte[] all = new byte[head.length + body.length];
        System.arraycopy(head, 0, all, 0, head.length);
        System.arraycopy(body, 0, all, head.length, body.length);
        return all;
    }
}