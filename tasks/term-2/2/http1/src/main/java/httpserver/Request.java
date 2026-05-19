package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class Request {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final byte[] body;

    public Request(HttpMethod method, String path, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body != null ? body.clone() : new byte[0];
    }

    public HttpMethod method() { return method; }
    public String path() { return path; }
    public Map<String, String> headers() { return headers; }

    public String header(String name) {
        return headers.get(name.toLowerCase());
    }

    public byte[] body() { return body.clone(); }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}