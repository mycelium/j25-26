package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Response {
    private int status;
    private String statusText;
    private final Map<String, String> headers;
    private byte[] body;
    
    public Response() {
        this.status = 200;
        this.statusText = "OK";
        this.headers = new LinkedHashMap<>();
        this.headers.put("Content-Type", "text/plain; charset=utf-8");
        this.body = new byte[0];
    }
    
    public Response status(int code, String text) {
        this.status = code;
        this.statusText = text;
        return this;
    }
    
    public Response status(int code) {
        this.status = code;
        this.statusText = getDefaultMessage(code);
        return this;
    }
    
    public Response header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    
    public Response text(String content) {
        this.body = content.getBytes(StandardCharsets.UTF_8);
        this.headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }
    
    public Response bytes(byte[] data) {
        this.body = data;
        this.headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }
    
    private String getDefaultMessage(int code) {
        switch(code) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
    
    public int getStatus() { return status; }
    public String getStatusText() { return statusText; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
}
