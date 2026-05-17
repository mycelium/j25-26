package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Request {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private final List<MultipartPart> parts;

    Request(HttpMethod method,
            String path,
            Map<String, String> headers,
            Map<String, String> queryParams,
            byte[] body,
            List<MultipartPart> parts) {

        this.method = method;
        this.path = path;

        Map<String, String> h = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        h.putAll(headers);
        this.headers = Collections.unmodifiableMap(h);

        this.queryParams = Collections.unmodifiableMap(new TreeMap<>(queryParams));
        this.body  = body.clone();
        this.parts = List.copyOf(parts);
    }
    public HttpMethod getMethod() { return method; }
    public String getPath() { return path; }

    public String getHeader(String name) { return headers.get(name); }

    public Map<String, String> getHeaders() { return headers; }

    public String getParam(String name) { return queryParams.get(name); }
    public Map<String, String> getQueryParams() { return queryParams; }

    public byte[] getBody() { return body.clone(); }

    public String getBodyAsString() { return new String(body, StandardCharsets.UTF_8); }

    public List<MultipartPart> getParts() { return parts; }

    public boolean isMultipart() { return !parts.isEmpty(); }

    @Override
    public String toString() { return method + " " + path; }
}
