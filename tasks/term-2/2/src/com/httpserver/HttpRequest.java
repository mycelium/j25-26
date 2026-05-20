package com.httpserver;

import java.util.*;

public final class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final byte[] body;
    private final Map<String, String> headers;
    private final Map<String, List<String>> queryParams;

    HttpRequest(HttpMethod method, String path, String version, byte[] body,
                Map<String, String> headers, Map<String, List<String>> queryParams) {
        this.method      = method;
        this.path        = path;
        this.version     = version;
        this.body        = body == null ? new byte[0] : body.clone();
        this.headers     = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(queryParams));
    }

    public HttpMethod getMethod()  { return method; }
    public String getPath()        { return path; }
    public String getVersion()     { return version; }

    public byte[] getBody()        { return body.clone(); }

    public String getBodyAsString() {
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }

     public Optional<String> getHeader(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase()));
    }

     public List<String> getQueryParam(String key) {
        return queryParams.getOrDefault(key, List.of());
    }

    public Optional<String> getFirstQueryParam(String key) {
        List<String> values = queryParams.getOrDefault(key, List.of());
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    @Override
    public String toString() {
        return method + " " + path + " " + version;
    }
}