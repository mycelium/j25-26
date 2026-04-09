package http;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public static HttpResponse ok(String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new HttpResponse(200, "OK", headers, body);
    }

    public static HttpResponse notFound(String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new HttpResponse(404, "Not Found", headers, body);
    }

    public static HttpResponse badRequest(String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new HttpResponse(400, "Bad Request", headers, body);
    }

    public static HttpResponse internalServerError(String body) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "text/plain; charset=UTF-8");
        return new HttpResponse(500, "Internal Server Error", headers, body);
    }
}