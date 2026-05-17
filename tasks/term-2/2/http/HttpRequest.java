package http;

import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> formData;
    private final Map<String, String> queryParams;

    public HttpRequest(String method, String path, Map<String, String> headers,
                       String body, Map<String, String> formData, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.formData = formData;
        this.queryParams = queryParams;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    public Map<String, String> getFormData() { return formData; }
    public Map<String, String> getQueryParams() { return queryParams; }
}
