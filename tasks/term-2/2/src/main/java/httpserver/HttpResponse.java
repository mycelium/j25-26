package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable HTTP response — populate it inside your {@link HttpServer.Handler},
 * the framework serialises it to wire bytes automatically.
 *
 * <pre>{@code
 * server.addRoute(HttpMethod.GET, "/hello", (req, res) -> {
 *     res.status(200, "OK").body("Hello!");
 * });
 * }</pre>
 */
public class HttpResponse {

    private int statusCode       = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    // ── Status ─────────────────────────────────────────────────────────────────

    public HttpResponse status(int code, String message) {
        this.statusCode    = code;
        this.statusMessage = message;
        return this;
    }

    public int    getStatusCode()    { return statusCode; }
    public String getStatusMessage() { return statusMessage; }

    // ── Headers ────────────────────────────────────────────────────────────────

    public HttpResponse header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /** All response headers, accessible as a {@link Map}. */
    public Map<String, String> getHeaders() { return headers; }

    // ── Body ───────────────────────────────────────────────────────────────────

    public HttpResponse body(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public HttpResponse body(byte[] bytes) {
        this.body = bytes;
        return this;
    }

    public HttpResponse json(String json) {
        headers.put("Content-Type", "application/json; charset=utf-8");
        return body(json);
    }

    public byte[] getBody() { return body; }

    // ── Serialisation ──────────────────────────────────────────────────────────

    /** Serialises to raw HTTP/1.1 bytes ready to send over the wire. */
    public byte[] toBytes() {
        headers.put("Content-Length", String.valueOf(body.length));

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(' ').append(statusMessage).append("\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("Connection: close\r\n\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(body,        0, result, headerBytes.length, body.length);
        return result;
    }
}
