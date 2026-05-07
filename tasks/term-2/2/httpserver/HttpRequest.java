package httpserver;

import java.util.Map;

public class HttpRequest {
    private final RequestMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> queryParams;
    private final Map<String, String> formData;

    public HttpRequest(RequestMethod method, String path, Map<String, String> headers,
                       byte[] body, Map<String, String> queryParams, Map<String, String> formData) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams;
        this.formData = formData;
    }

    public RequestMethod getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body); }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getFormData() { return formData; }
}