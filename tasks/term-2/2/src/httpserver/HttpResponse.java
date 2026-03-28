package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = new LinkedHashMap<>(headers);
        this.body = body == null ? new byte[0] : body.clone();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return new LinkedHashMap<>(headers);
    }

    public byte[] getBody() {
        return body.clone();
    }

    public static HttpResponse text(int statusCode, String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new HttpResponse(statusCode, headers, body.getBytes(StandardCharsets.UTF_8));
    }
}
