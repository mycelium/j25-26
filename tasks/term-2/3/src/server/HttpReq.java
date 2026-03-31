package server;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Collections;

public class HttpReq {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private byte[] body;

    public HttpReq(InputStream is) throws IOException {
        String requestLine = readLine(is);
        if (requestLine == null || requestLine.isEmpty()) throw new IOException("Empty request");

        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        String fullPath = parts[1];

        if (fullPath.contains("?")) {
            String[] pathParts = fullPath.split("\\?", 2);
            this.path = pathParts[0];
            parseQueryParams(pathParts[1]);
        } else {
            this.path = fullPath;
        }

        String line;
        while ((line = readLine(is)) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Length"));
            this.body = is.readNBytes(length);
        } else {
            this.body = new byte[0];
        }
    }

    private String readLine(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            if (b == '\r') {
                int next = is.read();
                if (next == '\n') break;
                buf.write(b);
                buf.write(next);
            } else if (b == '\n') {
                break;
            } else {
                buf.write(b);
            }
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    private void parseQueryParams(String query) {
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBodyBytes() { return body; }
    public String getBody() { return new String(body, StandardCharsets.UTF_8); }
    public Map<String, String> getQueryParams() { return queryParams; }
    
    public List<MultipartPart> getParts() {
        String contentType = headers.get("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return Collections.emptyList();
        }

        String boundary = null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring("boundary=".length());
                break;
            }
        }

        if (boundary == null) return Collections.emptyList();

        return MultipartParser.parse(body, boundary);
    }
    
    
    
}
