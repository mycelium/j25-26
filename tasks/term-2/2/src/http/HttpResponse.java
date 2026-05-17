package http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponse {

    private int statusCode = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public int statusCode() {
        return statusCode;
    }

    public HttpResponse status(int code) {
        this.statusCode = code;
        return this;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public HttpResponse header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public byte[] body() {
        return body.clone();
    }

    public HttpResponse writeBytes(byte[] data) {
        this.body = data == null ? new byte[0] : data.clone();
        return this;
    }

    public HttpResponse writeText(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "text/plain; charset=utf-8");
        }
        return this;
    }
}
