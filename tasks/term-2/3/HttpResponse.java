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
    public String getStatusMessage() { return statusMessage; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public void send(String responseBody) {
        this.body = responseBody;
        headers.put("Content-Length", String.valueOf(responseBody.length()));
    }

    public void status(int code) {
        this.statusCode = code;
        switch (code) {
            case 200: this.statusMessage = "OK"; break;
            case 404: this.statusMessage = "Not Found"; break;
            case 500: this.statusMessage = "Internal Server Error"; break;
            default:  this.statusMessage = "Unknown";
        }
    }
}
