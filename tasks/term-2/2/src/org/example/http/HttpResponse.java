package org.example.http;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public void setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "text/plain; charset=utf-8");
        }
        headers.put("Content-Length", String.valueOf(this.body.length));
    }

    public void setJsonBody(String json) {
        this.body = json.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", String.valueOf(this.body.length));
    }

    // Преобразование ответа в байты для отправки
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String statusLine = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n";
        
        try {
            baos.write(statusLine.getBytes(StandardCharsets.UTF_8));
            
            // Default headers if not set
            if (!headers.containsKey("Connection")) {
                headers.put("Connection", "close");
            }
            if (!headers.containsKey("Content-Length") && body != null) {
                 headers.put("Content-Length", String.valueOf(body.length));
            }

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
                baos.write(headerLine.getBytes(StandardCharsets.UTF_8));
            }

            baos.write("\r\n".getBytes(StandardCharsets.UTF_8)); // End of headers
            
            if (body != null) {
                baos.write(body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error building response", e);
        }
        
        return baos.toByteArray();
    }
}