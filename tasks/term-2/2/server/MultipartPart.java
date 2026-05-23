package server;

import java.util.Map;

public class MultipartPart {
    private final Map<String, String> headers;
    private final byte[] body;

    public MultipartPart(Map<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body);
    }

    public String getName() {
        return extractParam("content-disposition", "name");
    }

    public String getFilename() {
        return extractParam("content-disposition", "filename");
    }

    public String getContentType() {
        return headers.get("content-type");
    }

    private String extractParam(String header, String param) {
        String value = headers.get(header);
        if (value == null) return null;
        String search = param + "=\"";
        int start = value.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = value.indexOf('"', start);
        if (end < 0) return null;
        return value.substring(start, end);
    }
}
