package com.httpserver;

import java.util.*;

public final class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final byte[] body;
    private final Map<String, String> headers;
    private final Map<String, List<String>> queryParams;

    // Constructeur package-private : seul HttpRequestParser crée des requêtes
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

    /** Retourne une copie défensive du corps. */
    public byte[] getBody()        { return body.clone(); }

    /** Retourne le corps sous forme de String UTF-8. */
    public String getBodyAsString() {
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Recherche insensible à la casse (les noms sont stockés en lowercase). */
    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase()));
    }

    /** Retourne toutes les valeurs d'un query param (liste vide si absent). */
    public List<String> getQueryParam(String key) {
        return queryParams.getOrDefault(key, List.of());
    }

    /** Retourne la première valeur d'un query param. */
    public Optional<String> getFirstQueryParam(String key) {
        List<String> values = queryParams.getOrDefault(key, List.of());
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    @Override
    public String toString() {
        return method + " " + path + " " + version;
    }
}
