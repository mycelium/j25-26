package com.webserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private String statusText;
    private final Map<String, String> headers;
    private byte[] bodyData;

    public Response() {
        this.statusCode = 200;
        this.statusText = "OK";
        this.headers = new LinkedHashMap<>();
        this.bodyData = new byte[0];
        this.headers.put("Content-Length", "0");
    }

    public Response status(int code, String text) {
        this.statusCode = code;
        this.statusText = text;
        return this;
    }

    public Response header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Response body(String content) {
        this.bodyData = content.getBytes(StandardCharsets.UTF_8);
        this.headers.put("Content-Length", String.valueOf(this.bodyData.length));
        return this;
    }

    public Response body(byte[] content) {
        this.bodyData = content;
        this.headers.put("Content-Length", String.valueOf(content.length));
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return bodyData;
    }

    public byte[] serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ")
                .append(statusCode)
                .append(' ')
                .append(statusText)
                .append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        sb.append("\r\n");

        byte[] head = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
        byte[] result = new byte[head.length + bodyData.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(bodyData, 0, result, head.length, bodyData.length);
        return result;
    }
}
