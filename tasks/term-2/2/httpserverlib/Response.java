package httpserverlib;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private int status = 200;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public int getStatus() {
        return status;
    }

    public void setStatus(int newStatus) {
        this.status = newStatus;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String newName, String newValue) {
        headers.put(newName, newValue);
    }

    public byte[] getBody() {
        return body.clone();
    }

    public void setBody(byte[] newBody) {
        this.body = newBody.clone();
    }

    public void setBody(String newBody) {
        this.body = newBody.getBytes();
    }
    
}
