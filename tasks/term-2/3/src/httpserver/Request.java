package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

public class Request {
    private final String method;
    private final String uri;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final List<FormPart> formParts;

    public Request(String method, String uri, String path, Map<String, String> queryParams,
                   Map<String, String> headers, byte[] body, List<FormPart> formParts) {
        this.method = method;
        this.uri = uri;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
        this.formParts = formParts;
    }
    
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getPath() { return path; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBodyBytes() { return body; }
    public String getBodyText() { return body != null ? new String(body, StandardCharsets.UTF_8) : ""; }
    public List<FormPart> getFormParts() { return formParts; }
    public String getHeader(String name) { return headers.get(name.toLowerCase()); }
}
