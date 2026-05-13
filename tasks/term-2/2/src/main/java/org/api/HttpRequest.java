package org.api;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class HttpRequest {

    private final HttpMethod          method;
    private final String              path;
    private final String              queryString;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[]              body;
    private final List<MultipartPart> parts;

    public HttpRequest(HttpMethod method,
                       String path,
                       String queryString,
                       Map<String, String> headers,
                       Map<String, String> queryParams,
                       byte[] body,
                       List<MultipartPart> parts) {
        this.method      = method;
        this.path        = path;
        this.queryString = queryString;
        this.headers     = Collections.unmodifiableMap(
                               new TreeMap<>(String.CASE_INSENSITIVE_ORDER) {{ putAll(headers); }});
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.body        = body.clone();
        this.parts       = List.copyOf(parts);
    }

    public HttpMethod          method()       { return method; }
    public String              path()         { return path; }
    public String              queryString()  { return queryString; }
    public Map<String, String> headers()      { return headers; }
    public Map<String, String> queryParams()  { return queryParams; }
    public byte[]              body()         { return body.clone(); }
    public String              bodyAsString() { return new String(body, StandardCharsets.UTF_8); }
    public List<MultipartPart> parts()        { return parts; }

    public String header(String name)     { return headers.get(name); }
    public String queryParam(String name) { return queryParams.get(name); }
}
