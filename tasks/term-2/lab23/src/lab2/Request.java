package lab2;
import java.util.HashMap;
import java.util.Map;

public class Request {
    
    private String method;
    private String path;
    private Map<String, String> headers;
    private String body;
    
    public Request() {
        this.headers = new HashMap<>();
        this.body = "";
    }
    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public String getBody() {
        return body;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}