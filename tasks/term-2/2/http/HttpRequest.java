package http;
import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final String body;
    private final Map<String, String> headers;
    private Map<String, String> formFields = new HashMap<>();
    private Map<String, byte[]> fileParts = new HashMap<>();

    public HttpRequest(String method, String path, String version, String body, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
        this.headers = headers;
    }

    public HttpRequest(String method, String path, String version, String body, Map<String, String> headers,
                        Map<String, String> formFields, Map<String, byte[]> fileParts) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
        this.headers = headers;
        this.formFields = formFields;
        this.fileParts = fileParts;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getFormFields() {
        return formFields;
    }

    public Map<String, byte[]> getFileParts() {
        return fileParts;
    }


}
