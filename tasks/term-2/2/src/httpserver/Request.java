package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String uri;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final List<FormPart> formParts;

    public Request(String method, String uri, Map<String, String> headers, 
                   byte[] body, List<FormPart> formParts) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
        this.formParts = formParts;
        
        int qmIdx = uri.indexOf('?');
        if (qmIdx != -1) {
            this.path = uri.substring(0, qmIdx);
            this.queryParams = parseQueryString(uri.substring(qmIdx + 1));
        } else {
            this.path = uri;
            this.queryParams = Collections.emptyMap();
        }
    }
    
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(decode(kv[0]), kv.length > 1 ? decode(kv[1]) : "");
        }
        return params;
    }
    
    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
    
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getPath() { return path; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBodyBytes() { return body; }
    public String getBodyText() { return body != null ? new String(body, StandardCharsets.UTF_8) : ""; }
    public List<FormPart> getFormParts() { return formParts; }
    
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }
}
