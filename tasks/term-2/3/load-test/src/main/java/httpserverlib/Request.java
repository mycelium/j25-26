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

    public Request(HttpMethod requestMethod,
                   String requestPath,
                   Map<String, String> requestHeaders,
                   byte[] requestBody,
                   Map<String, String> queryParams,
                   Map<String, String> multipartData) {
        this.method = requestMethod;
        this.path = requestPath;
        this.headers = Collections.unmodifiableMap(requestHeaders);
        this.body = requestBody.clone();
        this.parameters = Collections.unmodifiableMap(queryParams);
        this.multipartFields = Collections.unmodifiableMap(multipartData);
    }

    public String getBodyAsString() {
        return new String(body);
    }
}