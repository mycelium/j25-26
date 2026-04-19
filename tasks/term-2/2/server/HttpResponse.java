package server;

import java.util.*;

public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private final Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        this.statusCode = 200;
        this.statusMessage = "OK";
        this.headers = new LinkedHashMap<>();
        this.body = new byte[0];
    }

    public HttpResponse setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
        return this;
    }

    public HttpResponse setBody(String body) {
        this.body = body.getBytes();
        return this;
    }

    public HttpResponse setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public HttpResponse addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }

    byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusMessage).append("\r\n");
        sb.append("Content-Length: ").append(body.length).append("\r\n");
        sb.append("Connection: close\r\n");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes();
        byte[] result = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(body, 0, result, headerBytes.length, body.length);
        return result;
    }
}
