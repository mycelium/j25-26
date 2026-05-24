package httpserverlib;

import java.util.HashMap;
import java.util.Map;


public class Response {
    private int statusCode = 200;
    private final Map<String, String> responseHeaders = new HashMap<>();
    private byte[] responseBody = new byte[0];

    public int getStatus() {
        return statusCode;
    }

    public void setStatus(int newStatusCode) {
        this.statusCode = newStatusCode;
    }

    public Map<String, String> getHeaders() {
        return responseHeaders;
    }

    public void setHeader(String headerName, String headerValue) {
        responseHeaders.put(headerName, headerValue);
    }

    public byte[] getBody() {
        return responseBody.clone();
    }

    public void setBody(byte[] newBody) {
        this.responseBody = newBody.clone();
    }

    public void setBody(String textBody) {
        this.responseBody = textBody.getBytes();
    }
}