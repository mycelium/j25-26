package httpserver;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusText = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        headers.put("Content-Type", "text/plain");
    }

    public void setStatus(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes();
        headers.put("Content-Length", String.valueOf(this.body.length));
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusText() { return statusText; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
}