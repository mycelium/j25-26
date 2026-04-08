import java.util.*;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private Map<String, String> headers = new HashMap<>();
    private String body = "";

    public HttpResponse() {
        headers.put("Content-Type", "text/plain");
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getBody() { return body; }
    public void setBody(String body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length()));
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    //для быстрого ответа
    public void send(String responseBody) {
        this.body = responseBody;
        headers.put("Content-Length", String.valueOf(responseBody.length()));
    }

    public void sendJson(String jsonBody) {
        this.body = jsonBody;
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", String.valueOf(jsonBody.length()));
    }

    public void sendHtml(String htmlBody) {
        this.body = htmlBody;
        headers.put("Content-Type", "text/html");
        headers.put("Content-Length", String.valueOf(htmlBody.length()));
    }

    public void status(int code) {
        this.statusCode = code;
        switch(code) {
            case 200: this.statusMessage = "OK"; break;
            case 201: this.statusMessage = "Created"; break;
            case 400: this.statusMessage = "Bad Request"; break;
            case 404: this.statusMessage = "Not Found"; break;
            case 500: this.statusMessage = "Internal Server Error"; break;
            default: this.statusMessage = "Unknown";
        }
    }
}