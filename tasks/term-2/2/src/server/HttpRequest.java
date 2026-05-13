package server;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;

    HttpRequest(HttpMethod method, String path, Map<String, String> queryParams,
                Map<String, String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() { return method; }

    public String getPath() { return path; }

    public Map<String, String> getQueryParams() { return queryParams; }

    public Map<String, String> getHeaders() { return headers; }

    public byte[] getBody() { return body.clone(); }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }
}
