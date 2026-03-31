package server;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MultipartPart {
    private final Map<String, String> headers = new HashMap<>();
    private final byte[] body;

    public MultipartPart(Map<String, String> headers, byte[] body) {
        this.headers.putAll(headers);
        this.body = body;
    }

    public String getName() {
        return extractParam(headers.get("Content-Disposition"), "name");
    }

    public String getFilename() {
        return extractParam(headers.get("Content-Disposition"), "filename");
    }

    public String getValue() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public byte[] getBytes() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    private String extractParam(String header, String paramName) {
        if (header == null) return null;
        for (String part : header.split(";")) {
            part = part.trim();
            if (part.startsWith(paramName + "=")) {
                return part.substring(paramName.length() + 1).replace("\"", "");
            }
        }
        return null;
    }
}