package org.api;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponse {

    private final int                 statusCode;
    private final String              statusText;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private       byte[]              body    = new byte[0];

    private HttpResponse(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public static HttpResponse status(int code, String text) { return new HttpResponse(code, text); }
    public static HttpResponse ok()                          { return new HttpResponse(200, "OK"); }
    public static HttpResponse created()                     { return new HttpResponse(201, "Created"); }
    public static HttpResponse noContent()                   { return new HttpResponse(204, "No Content"); }
    public static HttpResponse badRequest()                  { return new HttpResponse(400, "Bad Request"); }
    public static HttpResponse notFound()                    { return new HttpResponse(404, "Not Found"); }
    public static HttpResponse methodNotAllowed()            { return new HttpResponse(405, "Method Not Allowed"); }
    public static HttpResponse internalServerError()         { return new HttpResponse(500, "Internal Server Error"); }

    public HttpResponse header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpResponse body(byte[] bytes) {
        this.body = bytes.clone();
        return this;
    }

    public HttpResponse body(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        return this.header("Content-Type", "text/plain; charset=utf-8");
    }

    public HttpResponse json(String json) {
        this.body = json.getBytes(StandardCharsets.UTF_8);
        return this.header("Content-Type", "application/json; charset=utf-8");
    }

    public int                 statusCode() { return statusCode; }
    public String              statusText() { return statusText; }
    public Map<String, String> headers()    { return headers; }
    public byte[]              body()       { return body; }
}
