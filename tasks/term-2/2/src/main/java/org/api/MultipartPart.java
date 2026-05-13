package org.api;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class MultipartPart {

    private final Map<String, String> headers;
    private final byte[]              body;

    public MultipartPart(Map<String, String> headers, byte[] body) {
        this.headers = Map.copyOf(headers);
        this.body    = body.clone();
    }

    public Map<String, String> headers()      { return headers; }
    public byte[]              body()         { return body.clone(); }
    public String              bodyAsString() { return new String(body, StandardCharsets.UTF_8); }

    public String contentDisposition() { return headers.getOrDefault("content-disposition", ""); }

    public String name()     { return extractDispositionParam("name"); }
    public String filename() { return extractDispositionParam("filename"); }

    private String extractDispositionParam(String param) {
        for (String part : contentDisposition().split(";")) {
            part = part.trim();
            if (part.startsWith(param + "=")) {
                return part.substring(param.length() + 1).replace("\"", "").trim();
            }
        }
        return null;
    }
}
