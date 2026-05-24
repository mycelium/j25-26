package lab2.http;

import java.util.Map;

public final class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> multipartFields;

    public HttpRequest(
            String method,
            String path,
            String protocol,
            Map<String, String> headers,
            String body,
            Map<String, String> multipartFields
    ) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = Map.copyOf(headers);
        this.body = body == null ? "" : body;
        this.multipartFields = Map.copyOf(multipartFields);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        if (name == null) {
            return null;
        }
        return headers.get(name.toLowerCase());
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getMultipartFields() {
        return multipartFields;
    }
}
