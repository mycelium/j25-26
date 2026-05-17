package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Response {

    private int status = 200;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public int getStatus() { return status; }
    public Response setStatus(int code) { this.status = code; return this; }

    public Map<String, String> getHeaders() { return headers; }
    public String getHeader(String name) { return headers.get(name); }
    public Response  setHeader(String name, String v) { headers.put(name, v); return this; }

    public byte[] getBody() { return body.clone(); }

    public Response setBody(byte[] bytes) {
        this.body = bytes.clone();
        return this;
    }

    public Response setBody(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        headers.putIfAbsent("Content-Type", "text/plain; charset=utf-8");
        return this;
    }

    public Response setJson(String json) {
        this.body = json.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return this;
    }

    public Response setHtml(String html) {
        this.body = html.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Type", "text/html; charset=utf-8");
        return this;
    }
}
