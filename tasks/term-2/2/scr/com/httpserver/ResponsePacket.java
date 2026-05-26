package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponsePacket {
    private int code = 200;
    private String message = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] content = new byte[0];

    public ResponsePacket() {
        headers.put("Content-Length", "0");
    }

    public ResponsePacket withStatus(int code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public ResponsePacket withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public ResponsePacket withBody(String body) {
        this.content = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(this.content.length));
        return this;
    }

    public ResponsePacket withBody(byte[] body) {
        this.content = body;
        headers.put("Content-Length", String.valueOf(body.length));
        return this;
    }

    public int getStatusCode() { return code; }
    public String getStatusMessage() { return message; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getContent() { return content; }

    public byte[] serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(code).append(' ').append(message).append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        builder.append("\r\n");

        byte[] headerBytes = builder.toString().getBytes(StandardCharsets.ISO_8859_1);
        byte[] result = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(content, 0, result, headerBytes.length, content.length);
        return result;
    }
}