package server;

import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;
    private Map<String, String> pathParams;
    private List<MultipartPart> multipartParts;

    private HttpRequest(String method, String path, Map<String, String> headers,
                        Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
        this.pathParams = new LinkedHashMap<>();
    }

    static HttpRequest parse(byte[] data) {
        int headerEnd = findSequence(data, new byte[]{'\r', '\n', '\r', '\n'}, 0);

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
        int qIdx = fullPath.indexOf('?');
        if (qIdx >= 0) {
            path = fullPath.substring(0, qIdx);
            for (String param : fullPath.substring(qIdx + 1).split("&")) {
                String[] kv = param.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon >= 0) {
                String name = lines[i].substring(0, colon).trim().toLowerCase();
                String value = lines[i].substring(colon + 1).trim();
                headers.put(name, value);
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
        int pos = findSequence(bodyBytes, boundaryBytes, 0);

        while (pos >= 0) {
            pos += boundaryBytes.length;
            if (pos + 1 < bodyBytes.length && bodyBytes[pos] == '-' && bodyBytes[pos + 1] == '-') break;
            if (pos + 1 < bodyBytes.length && bodyBytes[pos] == '\r' && bodyBytes[pos + 1] == '\n') pos += 2;

            byte[] nextBoundaryPrefix = ("\r\n" + boundary).getBytes();
            int nextBoundary = findSequence(bodyBytes, nextBoundaryPrefix, pos);
            if (nextBoundary < 0) break;

            byte[] partData = Arrays.copyOfRange(bodyBytes, pos, nextBoundary);
            MultipartPart part = parseMultipartPart(partData);
            if (part != null) multipartParts.add(part);

            pos = nextBoundary + 2;
        }
    }

    private MultipartPart parseMultipartPart(byte[] data) {
        int headerEnd = findSequence(data, new byte[]{'\r', '\n', '\r', '\n'}, 0);
        if (headerEnd < 0) return null;

        Map<String, String> partHeaders = new LinkedHashMap<>();
        for (String line : new String(data, 0, headerEnd).split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon >= 0) {
                partHeaders.put(line.substring(0, colon).trim().toLowerCase(),
                        line.substring(colon + 1).trim());
            }
        }
        byte[] partBody = Arrays.copyOfRange(data, headerEnd + 4, data.length);
        return new MultipartPart(partHeaders, partBody);
    }

    private static int findSequence(byte[] data, byte[] pattern, int start) {
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

    void setPathParams(Map<String, String> pathParams) { this.pathParams = pathParams; }
}
