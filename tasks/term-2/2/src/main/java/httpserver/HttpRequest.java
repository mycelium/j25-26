package httpserver;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Immutable representation of an HTTP request.
 * Multipart parts are represented by the nested {@link Part} class.
 */
public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String queryString;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] rawBody;
    private final List<Part> multipartParts;

    private HttpRequest(Builder b) {
        this.method        = b.method;
        this.path          = b.path;
        this.queryString   = b.queryString;
        this.queryParams   = Collections.unmodifiableMap(b.queryParams);
        this.headers       = Collections.unmodifiableMap(b.headers);
        this.rawBody       = b.rawBody;
        this.multipartParts = Collections.unmodifiableList(b.multipartParts);
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public HttpMethod getMethod()                  { return method; }
    public String     getPath()                    { return path; }
    public String     getQueryString()             { return queryString; }

    /** All query parameters parsed from the URL. */
    public Map<String, String> getQueryParams()    { return queryParams; }
    public String getQueryParam(String name)       { return queryParams.get(name); }

    /**
     * All request headers as a case-insensitive map (keys stored in lower-case).
     * Satisfies: "Headers (should be accessible as Map)".
     */
    public Map<String, String> getHeaders()        { return headers; }

    /** Single header by name — lookup is case-insensitive. */
    public String getHeader(String name)           { return headers.get(name.toLowerCase()); }

    public byte[]  getRawBody()                    { return rawBody; }
    public String  getBody()                       { return new String(rawBody, StandardCharsets.UTF_8); }

    /** Parsed multipart parts; empty list when the request is not multipart. */
    public List<Part> getMultipartParts()          { return multipartParts; }
    public Optional<Part> getMultipartPart(String name) {
        return multipartParts.stream().filter(p -> name.equals(p.getName())).findFirst();
    }
    public boolean isMultipart()                   { return !multipartParts.isEmpty(); }

    // ── Nested: multipart part ─────────────────────────────────────────────────

    /**
     * A single part from a {@code multipart/form-data} body.
     */
    public static class Part {

        private final Map<String, String> headers;
        private final String name;
        private final String filename;
        private final String contentType;
        private final byte[] data;

        Part(Map<String, String> headers, byte[] data) {
            this.headers     = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
            this.data        = data;

            String disposition = headers.getOrDefault("content-disposition", "");
            this.name        = extractParam(disposition, "name");
            this.filename    = extractParam(disposition, "filename");
            this.contentType = headers.getOrDefault("content-type", "text/plain");
        }

        private static String extractParam(String header, String param) {
            String search = param + "=\"";
            int idx = header.indexOf(search);
            if (idx == -1) return null;
            int start = idx + search.length();
            int end   = header.indexOf('"', start);
            return end == -1 ? null : header.substring(start, end);
        }

        public String getName()              { return name; }
        public String getFilename()          { return filename; }
        public String getContentType()       { return contentType; }
        public byte[] getData()              { return data; }
        public Map<String, String> getHeaders() { return headers; }
        public boolean isFile()              { return filename != null; }
        public String getBodyAsString()      { return new String(data, StandardCharsets.UTF_8); }
    }

    // ── Builder ────────────────────────────────────────────────────────────────

    static class Builder {
        HttpMethod method;
        String path        = "/";
        String queryString = "";
        final Map<String, String> queryParams   = new LinkedHashMap<>();
        final Map<String, String> headers       = new LinkedHashMap<>();
        byte[] rawBody                          = new byte[0];
        final List<Part> multipartParts         = new ArrayList<>();

        Builder method(HttpMethod m)          { this.method = m;        return this; }
        Builder path(String p)                { this.path = p;          return this; }
        Builder queryString(String qs)        { this.queryString = qs;  return this; }
        Builder queryParam(String k, String v){ queryParams.put(k, v);  return this; }
        Builder header(String k, String v)    { headers.put(k.toLowerCase(), v); return this; }
        Builder rawBody(byte[] b)             { this.rawBody = b;       return this; }
        Builder multipartParts(List<Part> ps) { multipartParts.addAll(ps); return this; }

        HttpRequest build() {
            Objects.requireNonNull(method, "method must not be null");
            return new HttpRequest(this);
        }
    }

    // ── Static factory: parse raw bytes ───────────────────────────────────────

    /**
     * Parse a raw HTTP/1.1 request from bytes.
     * Called by {@link HttpRequestParser}.
     */
    static HttpRequest parse(byte[] raw) throws Exception {
        byte[] sep = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        int sepIdx = indexOf(raw, sep, 0);
        if (sepIdx == -1) throw new Exception("Malformed HTTP request: no header/body separator");

        String headerSection = new String(raw, 0, sepIdx, StandardCharsets.UTF_8);
        int bodyStart = sepIdx + sep.length;
        byte[] rawBody = Arrays.copyOfRange(raw, bodyStart, raw.length);

        String[] lines = headerSection.split("\r\n");
        if (lines.length == 0) throw new Exception("Empty request");

        // Request line
        String[] rl = lines[0].split(" ", 3);
        if (rl.length < 2) throw new Exception("Invalid request line: " + lines[0]);

        HttpMethod method = HttpMethod.fromString(rl[0]);
        String fullPath   = rl[1];

        String path, queryString = "";
        Map<String, String> queryParams = new LinkedHashMap<>();
        int qIdx = fullPath.indexOf('?');
        if (qIdx != -1) {
            path        = fullPath.substring(0, qIdx);
            queryString = fullPath.substring(qIdx + 1);
            queryParams = parseQueryString(queryString);
        } else {
            path = fullPath;
        }

        // Headers
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon == -1) continue;
            headers.put(lines[i].substring(0, colon).trim().toLowerCase(),
                        lines[i].substring(colon + 1).trim());
        }

        Builder builder = new Builder()
                .method(method).path(path).queryString(queryString).rawBody(rawBody);
        queryParams.forEach(builder::queryParam);
        headers.forEach(builder::header);

        // Multipart
        String ct = headers.getOrDefault("content-type", "");
        if (ct.toLowerCase().contains("multipart/form-data")) {
            String boundary = extractBoundary(ct);
            if (boundary != null) builder.multipartParts(parseMultipart(rawBody, boundary));
        }

        return builder.build();
    }

    // ── Parsing helpers ────────────────────────────────────────────────────────

    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> params = new LinkedHashMap<>();
        if (qs == null || qs.isEmpty()) return params;
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            try {
                if (eq == -1) {
                    params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                } else {
                    params.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                               URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
                }
            } catch (Exception ignored) {}
        }
        return params;
    }

    private static String extractBoundary(String contentType) {
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) return part.substring("boundary=".length()).trim();
        }
        return null;
    }

    private static List<Part> parseMultipart(byte[] body, String boundary) {
        List<Part> parts    = new ArrayList<>();
        byte[] delimiter    = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] closeDelim   = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
        byte[] headerSep    = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        int pos = 0;
        while (pos < body.length) {
            int delimIdx = indexOf(body, delimiter, pos);
            if (delimIdx == -1 || startsWith(body, closeDelim, delimIdx)) break;

            int partStart = delimIdx + delimiter.length;
            if (partStart + 1 < body.length && body[partStart] == '\r' && body[partStart + 1] == '\n') {
                partStart += 2;
            } else { pos = partStart; continue; }

            int headerEnd = indexOf(body, headerSep, partStart);
            if (headerEnd == -1) break;

            Map<String, String> partHeaders = new LinkedHashMap<>();
            for (String line : new String(body, partStart, headerEnd - partStart, StandardCharsets.UTF_8).split("\r\n")) {
                int c = line.indexOf(':');
                if (c == -1) continue;
                partHeaders.put(line.substring(0, c).trim().toLowerCase(), line.substring(c + 1).trim());
            }

            int bodyStartIdx = headerEnd + headerSep.length;
            int nextDelim    = indexOf(body, delimiter, bodyStartIdx);
            if (nextDelim == -1) break;

            int bodyEnd = nextDelim;
            if (bodyEnd >= 2 && body[bodyEnd - 2] == '\r' && body[bodyEnd - 1] == '\n') bodyEnd -= 2;

            parts.add(new Part(partHeaders, Arrays.copyOfRange(body, bodyStartIdx, bodyEnd)));
            pos = nextDelim;
        }
        return parts;
    }

    // ── Byte utils ─────────────────────────────────────────────────────────────

    static int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = from; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static boolean startsWith(byte[] data, byte[] prefix, int offset) {
        if (offset + prefix.length > data.length) return false;
        for (int i = 0; i < prefix.length; i++) if (data[offset + i] != prefix[i]) return false;
        return true;
    }
}
