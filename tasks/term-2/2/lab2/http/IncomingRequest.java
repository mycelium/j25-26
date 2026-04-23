package lab2.http;

import java.util.Map;

public class IncomingRequest {
    private final String reqMethod;
    private final String uriTarget;
    private final Map<String, String> requestHeaders;
    private final String textBody;
    private final Map<String, String> parsedMultipart;

    public IncomingRequest(String reqMethod, String uriTarget, Map<String, String> requestHeaders, String textBody, Map<String, String> parsedMultipart) {
        this.reqMethod = reqMethod;
        this.uriTarget = uriTarget;
        this.requestHeaders = requestHeaders;
        this.textBody = textBody;
        this.parsedMultipart = parsedMultipart;
    }

    public String getReqMethod() { return reqMethod; }
    public String getUriTarget() { return uriTarget; }
    public Map<String, String> getRequestHeaders() { return requestHeaders; }
    public String getTextBody() { return textBody; }
    public Map<String, String> getParsedMultipart() { return parsedMultipart; }
}