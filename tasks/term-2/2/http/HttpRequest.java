package http;

import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> formData; 

    public HttpRequest(String method, String path, Map<String, String> headers, String body, Map<String, String> formData) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.formData = formData;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    public Map<String, String> getFormData() { return formData; } // <-- НОВЫЙ ГЕТТЕР
}