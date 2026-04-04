package httpserver;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final Server.Method method;
    private final String uri;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> formData;

    public Request(Server.Method method, String uri, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.uri = uri;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.body = body != null ? body : new byte[0];

        int q = uri.indexOf('?');
        if (q == -1) {
            this.path = uri;
            this.queryParams = Map.of();
        } else {
            this.path = uri.substring(0, q);
            this.queryParams = parseQueryString(uri.substring(q + 1));
        }

        this.formData = parseFormData();
    }

    private Map<String, String> parseFormData() {
        Map<String, String> result = new HashMap<>();
        String contentType = headers.get("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return result;
        }

        try {
            String boundary = "--" + contentType.split("boundary=")[1];
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            String[] parts = bodyStr.split(boundary);

            for (String part : parts) {
                if (part.isEmpty() || part.equals("--\r\n") || part.equals("--")) continue;
                int headerEnd = part.indexOf("\r\n\r\n");
                if (headerEnd == -1) continue;
                String partHeaders = part.substring(0, headerEnd);
                String partBody = part.substring(headerEnd + 4).trim();
                int nameIdx = partHeaders.indexOf("name=\"");
                if (nameIdx != -1) {
                    int nameEnd = partHeaders.indexOf("\"", nameIdx + 6);
                    String name = partHeaders.substring(nameIdx + 6, nameEnd);
                    result.put(name, partBody);
                }
            }
        } catch (Exception e) {
            // игнорируем ошибки парсинга multipart
        }
        return result;
    }

    private Map<String, String> parseQueryString(String qs) {
        Map<String, String> params = new HashMap<>();
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq == -1 ? pair : pair.substring(0, eq);
            String value = eq == -1 ? "" : pair.substring(eq + 1);
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignored) {}
            params.put(key, value);
        }
        return params;
    }

    public static Request parse(InputStream inputStream) throws IOException {
        // Читаем первую строку (request line)
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isEmpty())
            throw new IOException("Empty request");
        String[] parts = requestLine.split(" ");
        if (parts.length < 2)
            throw new IOException("Invalid request line");
        Server.Method method = Server.Method.valueOf(parts[0]);
        String uri = parts[1];

        // Читаем заголовки
        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = readLine(inputStream)).isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.put(key, value);
            }
        }

        // Читаем тело, если есть Content-Length
        byte[] body = null;
        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            body = new byte[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = inputStream.read(body, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }
        }

        return new Request(method, uri, headers, body);
    }

    private static String readLine(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        while ((c = is.read()) != -1) {
            if (c == '\r') {
                is.mark(1);
                int next = is.read();
                if (next == '\n') {
                    break;
                } else {
                    is.reset();
                    baos.write(c);
                }
            } else if (c == '\n') {
                break;
            } else {
                baos.write(c);
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    // Геттеры
    public Server.Method getMethod() { return method; }
    public String getUri() { return uri; }
    public String getPath() { return path; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body.clone(); }
    public Map<String, String> getFormData() { return formData; }
}