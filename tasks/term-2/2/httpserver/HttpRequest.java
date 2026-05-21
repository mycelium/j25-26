package httpserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> queryParams;

    public HttpRequest(HttpMethod method, String path, String version,
                       Map<String, String> headers, byte[] body,
                       Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.body = body != null ? body.clone() : new byte[0];
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(queryParams));
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public byte[] getBody() {
        return body.clone();
    }

    public String getBodyAsString() {
        return new String(body);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    @Override
    public String toString() {
        return method + " " + path + " " + version;
    }
}
