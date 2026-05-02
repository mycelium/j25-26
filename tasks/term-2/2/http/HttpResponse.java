package http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] payload = new byte[0];

    public int getStatusCode() { return statusCode; }
    
    public void setStatusCode(int code) { this.statusCode = code; }

    public Map<String, String> getHeaders() { return headers; }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public byte[] getPayload() { return payload.clone(); }

    public void setPayload(byte[] data) {
        this.payload = data.clone();
    }

    public void setPayload(String text) {
        this.payload = text.getBytes();
    }
}