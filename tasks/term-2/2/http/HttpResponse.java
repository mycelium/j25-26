package http;
import java.util.*;

public class HttpResponse {
    private int statusCode;
    private String body;
    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse() {} 

    public HttpResponse(int statusCode, String statusText, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 418: return "I'm a teapot";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            default: return "Unknown Status";
        }
    }

    public HttpResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
