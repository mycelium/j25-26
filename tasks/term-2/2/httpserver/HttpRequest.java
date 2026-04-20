package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String rawPath;
    private final String httpVersion;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final Map<String, List<String>> queryParams;

    public HttpRequest(HttpMethod method, String path, String rawPath, String httpVersion,
                       Map<String, List<String>> headers, byte[] body,
                       Map<String, List<String>> queryParams) {
        this.method = method;
        this.path = path;
        this.rawPath = rawPath;
        this.httpVersion = httpVersion;
        Map<String, List<String>> h = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        h.putAll(headers);
        this.headers = Collections.unmodifiableMap(h);
        this.body = body;
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(queryParams));
    }

    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }
    public String getRawPath() { return rawPath; }
    public String getHttpVersion() { return httpVersion; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body, StandardCharsets.UTF_8); }
    public Map<String, List<String>> getQueryParams() { return queryParams; }
    public List<String> getQueryParam(String name) { return queryParams.get(name); }
    public String getFirstQueryParam(String name) {
        List<String> vals = queryParams.get(name);
        return vals != null && !vals.isEmpty() ? vals.get(0) : null;
    }
    public String getHeader(String name) {
        List<String> vals = headers.get(name);
        return vals != null && !vals.isEmpty() ? vals.get(0) : null;
    }
}