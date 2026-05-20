import java.util.HashMap;
import java.util.Map;

public class Response {
    
    private int status;
    private String statusText;
    private Map<String, String> headers;
    private String body;
    
    public Response() {
        this.status = 200;
        this.statusText = "OK";
        this.headers = new HashMap<>();
        this.body = "";
        headers.put("Content-Type", "text/plain");
    }

    public void setStatus(int status) {
        this.status = status;
        switch (status) {
            case 200: statusText = "OK"; break;
            case 201: statusText = "Created"; break;
            case 204: statusText = "No Content"; break;
            case 400: statusText = "Bad Request"; break;
            case 404: statusText = "Not Found"; break;
            case 500: statusText = "Internal Server Error"; break;
            default: statusText = "Unknown";
        }
    }

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

    public String toHttpString() {
        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ").append(status).append(" ").append(statusText).append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        sb.append("\r\n");

        sb.append(body);
        
        return sb.toString();
    }
}