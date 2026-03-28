package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private final List<MultipartPart> multipartParts;

    HttpRequest(
            HttpMethod method,
            String path,
            String version,
            Map<String, String> headers,
            Map<String, String> queryParams,
            byte[] body,
            List<MultipartPart> multipartParts
    ) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(queryParams));
        this.body = body.clone();
        this.multipartParts = List.copyOf(multipartParts);
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

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public byte[] getBody() {
        return body.clone();
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public List<MultipartPart> getMultipartParts() {
        return multipartParts;
    }
}
