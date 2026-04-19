package server;

import java.util.Map;

public class MultipartPart {
    private final Map<String, String> headers;
    private final byte[] body;

    MultipartPart(Map<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    public String getName() {
        return extractDispositionParam("name");
    }

    public String getFilename() {
        return extractDispositionParam("filename");
    }

    public String getContentType() {
        return headers.get("content-type");
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

    private String extractDispositionParam(String param) {
        String disposition = headers.get("content-disposition");
        if (disposition == null) return null;
        String search = param + "=\"";
        int start = disposition.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = disposition.indexOf('"', start);
        return end == -1 ? null : disposition.substring(start, end);
    }
}
