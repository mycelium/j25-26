package http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Map<String, String> formFields;
    private final byte[] body;

    public HttpRequest(HttpMethod method,
                       String path,
                       String version,
                       Map<String, String> headers,
                       Map<String, String> queryParams,
                       Map<String, String> formFields,
                       byte[] body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = Collections.unmodifiableMap(headers);
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.formFields = Collections.unmodifiableMap(formFields);
        this.body = body == null ? new byte[0] : body.clone();
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String version() {
        return version;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, String> query() {
        return queryParams;
    }

    public Map<String, String> formFields() {
        return formFields;
    }

    public byte[] body() {
        return body.clone();
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
