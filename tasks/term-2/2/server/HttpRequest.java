package server;

import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private Map<String, String> pathParams = new LinkedHashMap<>();
    private List<MultipartPart> multipartParts;

    private HttpRequest(String method, String path, Map<String, String> headers,
                        Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
    }

    static HttpRequest parse(byte[] data) {
        int headerEnd = indexOf(data, new byte[]{'\r', '\n', '\r', '\n'}, 0);

        String headerSection;
        byte[] bodyBytes;
        if (headerEnd >= 0) {
            headerSection = new String(data, 0, headerEnd);
            bodyBytes = Arrays.copyOfRange(data, headerEnd + 4, data.length);
        } else {
            headerSection = new String(data);
            bodyBytes = new byte[0];
        }

        String[] lines = headerSection.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        String method = requestLine.length > 0 ? requestLine[0] : "GET";
        String fullPath = requestLine.length > 1 ? requestLine[1] : "/";

        String path = fullPath;
        Map<String, String> queryParams = new LinkedHashMap<>();
        int q = fullPath.indexOf('?');
        if (q >= 0) {
            path = fullPath.substring(0, q);
            for (String pair : fullPath.substring(q + 1).split("&")) {
                String[] kv = pair.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon >= 0) {
                headers.put(lines[i].substring(0, colon).trim().toLowerCase(),
                        lines[i].substring(colon + 1).trim());
            }
        }

        HttpRequest request = new HttpRequest(method, path, headers, queryParams, bodyBytes);

        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            request.parseMultipart(contentType, bodyBytes);
        }

        return request;
    }

    private void parseMultipart(String contentType, byte[] bodyBytes) {
        int boundaryIdx = contentType.indexOf("boundary=");
        if (boundaryIdx < 0) return;
        String boundary = "--" + contentType.substring(boundaryIdx + 9).trim();
        byte[] boundaryBytes = boundary.getBytes();

        multipartParts = new ArrayList<>();
        int pos = indexOf(bodyBytes, boundaryBytes, 0);

        while (pos >= 0) {
            pos += boundaryBytes.length;
            if (pos + 1 < bodyBytes.length && bodyBytes[pos] == '-' && bodyBytes[pos + 1] == '-') break;
            if (pos + 1 < bodyBytes.length && bodyBytes[pos] == '\r' && bodyBytes[pos + 1] == '\n') pos += 2;

            byte[] nextBoundaryPrefix = ("\r\n" + boundary).getBytes();
            int nextBoundary = indexOf(bodyBytes, nextBoundaryPrefix, pos);
            if (nextBoundary < 0) break;

            byte[] partData = Arrays.copyOfRange(bodyBytes, pos, nextBoundary);
            int partHeaderEnd = indexOf(partData, new byte[]{'\r', '\n', '\r', '\n'}, 0);
            if (partHeaderEnd >= 0) {
                Map<String, String> partHeaders = new LinkedHashMap<>();
                for (String line : new String(partData, 0, partHeaderEnd).split("\r\n")) {
                    int colon = line.indexOf(':');
                    if (colon >= 0) {
                        partHeaders.put(line.substring(0, colon).trim().toLowerCase(),
                                line.substring(colon + 1).trim());
                    }
                }
                multipartParts.add(new MultipartPart(partHeaders,
                        Arrays.copyOfRange(partData, partHeaderEnd + 4, partData.length)));
            }

            pos = nextBoundary + 2;
        }
    }

    private static int indexOf(byte[] data, byte[] pattern, int start) {
        outer:
        for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }
    public String getHeader(String name) { return headers.get(name.toLowerCase()); }
    public Map<String, String> getQueryParams() { return Collections.unmodifiableMap(queryParams); }
    public String getQueryParam(String name) { return queryParams.get(name); }
    public Map<String, String> getPathParams() { return Collections.unmodifiableMap(pathParams); }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body); }
    public boolean isMultipart() { return multipartParts != null; }
    public List<MultipartPart> getMultipartParts() {
        return multipartParts != null ? Collections.unmodifiableList(multipartParts) : Collections.emptyList();
    }

    void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }
}
