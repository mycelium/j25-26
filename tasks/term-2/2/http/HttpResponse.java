package http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(int statusCode, String statusMessage, String body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.headers = new HashMap<>();
        this.headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public int getStatusCode() { return statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
}