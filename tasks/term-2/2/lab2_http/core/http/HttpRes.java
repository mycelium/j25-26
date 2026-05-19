package core.http;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class HttpRes {
    private int statusCode = 200;
    private String statusText = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public void setStatus(int code) {
        this.statusCode = code;
        this.statusText = switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default  -> "OK";
        };
    }

    public void setStatus(int code, String text) {
        this.statusCode = code;
        this.statusText = text;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        addHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void setBody(byte[] body) {
        this.body = body;
        addHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void send(OutputStream os) throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(String.format("HTTP/1.1 %d %s\r\n", statusCode, statusText));

        headers.putIfAbsent("Content-Type", "text/plain; charset=utf-8");
        headers.forEach((k, v) -> headerBuilder.append(k).append(": ").append(v).append("\r\n"));
        headerBuilder.append("\r\n");

        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);
        
        os.write(headerBytes);
        os.write(body);
        os.flush();
    }
}