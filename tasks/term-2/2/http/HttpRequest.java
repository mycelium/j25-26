package http;

import java.util.Collections;
import java.util.Map;

public class HttpRequest {
    private final ReqMethod method;
    private final String route;
    private final Map<String, String> headers;
    private final byte[] payload;
    private final Map<String, String> queryParams;
    private final Map<String, String> formData;

    public HttpRequest(ReqMethod method, String route, Map<String, String> headers, 
                       byte[] payload, Map<String, String> queryParams, Map<String, String> formData) {
        this.method = method;
        this.route = route;
        this.headers = Collections.unmodifiableMap(headers);
        this.payload = payload != null ? payload.clone() : new byte[0];
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.formData = Collections.unmodifiableMap(formData);
    }

    public ReqMethod getMethod() { return method; }
    public String getRoute() { return route; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getPayload() { return payload.clone(); }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getFormData() { return formData; }

    public String getPayloadAsString() {
        return new String(payload);
    }
}