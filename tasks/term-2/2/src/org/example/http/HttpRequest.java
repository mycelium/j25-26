package org.example.http;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> queryParams;

    public HttpRequest(HttpMethod method, String path, String protocol,
                       Map<String, String> headers, byte[] body, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams; 
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getHeader(String name) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public List<FormPart> getFormParts() {
        String contentType = getHeader("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return Collections.emptyList();
        }

        String boundary = null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring(9);
                break;
            }
        }

        if (boundary == null) {
            return Collections.emptyList();
        }

        return parseMultipart(body, boundary);
    }

    private List<FormPart> parseMultipart(byte[] data, String boundary) {
        List<FormPart> parts = new ArrayList<>();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] endBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);

        int pos = indexOf(data, boundaryBytes, 0);
        if (pos == -1) return parts;

        while (true) {
            pos += boundaryBytes.length;

            if (pos + 1 < data.length && data[pos] == '-' && data[pos + 1] == '-') {
                break;
            }

            if (pos + 1 < data.length && data[pos] == '\r' && data[pos + 1] == '\n') {
                pos += 2;
            }

            Map<String, String> partHeaders = new HashMap<>();
            while (pos < data.length) {
                int lineEnd = indexOf(data, new byte[]{'\r', '\n'}, pos);
                if (lineEnd == -1 || lineEnd == pos) {
                    pos += 2;
                    break;
                }

                String headerLine = new String(data, pos, lineEnd - pos, StandardCharsets.UTF_8);
                String[] headerParts = headerLine.split(": ", 2);
                if (headerParts.length == 2) {
                    partHeaders.put(headerParts[0].trim(), headerParts[1].trim());
                }
                pos = lineEnd + 2;
            }

            int nextBoundary = indexOf(data, boundaryBytes, pos);
            if (nextBoundary == -1) break;

            int contentEnd = nextBoundary - 2;
            if (contentEnd < pos) {
                pos = nextBoundary;
                continue;
            }

            byte[] content = Arrays.copyOfRange(data, pos, contentEnd);
            parts.add(new FormPart(partHeaders, content));

            pos = nextBoundary;

            if (startsWith(data, endBoundaryBytes, pos)) {
                break;
            }
        }

        return parts;
    }

    private int indexOf(byte[] haystack, byte[] needle, int start) {
        for (int i = start; i <= haystack.length - needle.length; i++) {
            boolean match = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private boolean startsWith(byte[] array, byte[] prefix, int offset) {
        if (offset + prefix.length > array.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (array[offset + i] != prefix[i]) return false;
        }
        return true;
    }

    public static class FormPart {
        private final Map<String, String> headers;
        private final byte[] content;

        public FormPart(Map<String, String> headers, byte[] content) {
            this.headers = new HashMap<>(headers);
            this.content = content;
        }

        public String getName() {
            return extractParam("name");
        }

        public String getFileName() {
            return extractParam("filename");
        }

        public String getValue() {
            return new String(content, StandardCharsets.UTF_8);
        }

        public byte[] getContent() {
            return content;
        }

        private String extractParam(String paramName) {
            String disposition = headers.get("Content-Disposition");
            if (disposition == null) return null;

            for (String part : disposition.split(";")) {
                part = part.trim();
                if (part.startsWith(paramName + "=")) {
                    String value = part.substring(paramName.length() + 1);
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            }
            return null;
        }
    }
}