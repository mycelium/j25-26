package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {
    private int code = 200;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private byte[] content = new byte[0];

    public void setStatusCode(int code) { this.code = code; }

    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    public void setBody(String body) {
        this.content = body.getBytes(StandardCharsets.UTF_8);
    }

    public void setBody(byte[] data) { this.content = data; }

    byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String reason;
        switch (code) {
            // 2xx — Success
            case 200: reason = "OK"; break;
            case 201: reason = "Created"; break;
            case 204: reason = "No Content"; break;

            // 3xx — Redirection
            case 301: reason = "Moved Permanently"; break;
            case 302: reason = "Found"; break;
            case 304: reason = "Not Modified"; break;

            // 4xx — Client Error
            case 400: reason = "Bad Request"; break;
            case 401: reason = "Unauthorized"; break;
            case 403: reason = "Forbidden"; break;
            case 404: reason = "Not Found"; break;
            case 405: reason = "Method Not Allowed"; break;

            // 5xx — Server Error
            case 500: reason = "Internal Server Error"; break;
            case 502: reason = "Bad Gateway"; break;
            case 503: reason = "Service Unavailable"; break;

            default: reason = "Unknown"; break;
        }

        out.write(("HTTP/1.1 " + code + " " + reason + "\r\n")
                .getBytes(StandardCharsets.UTF_8));

        if (!headers.containsKey("Content-Length"))
            addHeader("Content-Length", String.valueOf(content.length));
        if (!headers.containsKey("Content-Type"))
            addHeader("Content-Type", "text/plain; charset=utf-8");

        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            for (String val : e.getValue()) {
                out.write((e.getKey() + ": " + val + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));

        if (code != 204 && code != 304 && content.length > 0)
            out.write(content);

        return out.toByteArray();
    }
}