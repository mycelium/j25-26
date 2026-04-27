package com.httpserverlib.model;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class HttpResponse {
    private HttpStatus status;
    private final Map<String, String> headers;
    private byte[] body;

    public HttpResponse() { this.status = HttpStatus.OK; this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); this.body = new byte[0]; }

    public static HttpResponse ok() { return new HttpResponse().status(HttpStatus.OK); }
    public static HttpResponse ok(String body) { return new HttpResponse().status(HttpStatus.OK).body(body); }
    public static HttpResponse ok(byte[] body) { return new HttpResponse().status(HttpStatus.OK).body(body); }
    public static HttpResponse created() { return new HttpResponse().status(HttpStatus.CREATED); }
    public static HttpResponse noContent() { return new HttpResponse().status(HttpStatus.NO_CONTENT); }
    public static HttpResponse badRequest() { return new HttpResponse().status(HttpStatus.BAD_REQUEST); }
    public static HttpResponse notFound() { return new HttpResponse().status(HttpStatus.NOT_FOUND); }
    public static HttpResponse internalServerError() { return new HttpResponse().status(HttpStatus.INTERNAL_SERVER_ERROR); }

    public HttpResponse status(HttpStatus status) { this.status = status; return this; }
    public HttpResponse header(String name, String value) { headers.put(name, value); return this; }
    public HttpResponse body(String body) { this.body = body.getBytes(StandardCharsets.UTF_8); header("Content-Type", "text/plain; charset=utf-8"); return this; }
    public HttpResponse body(byte[] body) { this.body = body.clone(); return this; }
    public HttpResponse json(String json) { this.body = json.getBytes(StandardCharsets.UTF_8); header("Content-Type", "application/json; charset=utf-8"); return this; }
    public HttpResponse html(String html) { this.body = html.getBytes(StandardCharsets.UTF_8); header("Content-Type", "text/html; charset=utf-8"); return this; }

    public HttpStatus getStatus() { return status; }
    public Map<String, String> getHeaders() { return Map.copyOf(headers); }
    public String getHeader(String name) { return headers.get(name); }
    public byte[] getBody() { return body.clone(); }
}