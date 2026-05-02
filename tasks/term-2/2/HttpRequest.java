import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private byte[] body;

    public HttpRequest(InputStream inputStream) throws IOException {
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        String fullPath = parts[1];

        if (fullPath.contains("?")) {
            String[] pathAndQuery = fullPath.split("\\?", 2);
            this.path = pathAndQuery[0];
            parseQueryParams(pathAndQuery[1]);
        } else {
            this.path = fullPath;
        }

        String headerLine;
        while ((headerLine = readLine(inputStream)) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            this.body = new byte[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int read = inputStream.read(this.body, totalRead, contentLength - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
        } else {
            this.body = new byte[0];
        }
    }

    private String readLine(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            if (b == '\r') {
                int next = is.read();
                if (next == '\n') {
                    break; 
                } else {
                    buffer.write(b);
                    buffer.write(next);
                }
            } else if (b == '\n') {
                break; 
            } else {
                buffer.write(b);
            }
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void parseQueryParams(String query) {
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length > 0) {
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
                queryParams.put(key, value);
            }
        }
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }
    public Map<String, String> getQueryParams() { return Collections.unmodifiableMap(queryParams); }
    public byte[] getBodyBytes() { return body; }
    public String getBodyAsString() { return new String(body, StandardCharsets.UTF_8); }
    
    public List<FormPart> getFormParts() {
        String contentType = headers.get("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return Collections.emptyList();
        }
        
        String boundary = null;
        String[] contentTypeParts = contentType.split(";");
        for (String part : contentTypeParts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring("boundary=".length());
                break;
            }
        }
        
        if (boundary == null) {
            return Collections.emptyList();
        }
        
        return FormDataParser.parse(body, boundary);
    }
}
