package httpserverlib;

import java.util.Collections;
import java.util.Map;

public class Request {
    public final HttpMethod method;
    public final String path;
    public final Map<String, String> headers;
    public final byte[] body;
    public final Map<String, String> parameters;
    public final Map<String, String> multipartFields;

    public Request(HttpMethod newMethod, String newPath, Map<String, String> newHeaders, byte[] newBody, Map<String, String> newParameters, Map<String, String> newMultipartFields) {
        this.method = newMethod;
        this.path = newPath;
        this.headers = Collections.unmodifiableMap(newHeaders);
        this.body = newBody.clone();
        this.parameters = Collections.unmodifiableMap(newParameters);
        this.multipartFields = Collections.unmodifiableMap(newMultipartFields);
    }
    
    public String getBodyAsString() {
        return new String(body);
    }
}