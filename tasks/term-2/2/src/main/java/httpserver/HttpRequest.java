package httpserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final Map<String, String> queryParams;

    HttpRequest(String method, String path,
                Map<String, List<String>> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
        this.queryParams = extractParams(path);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public InputStream getBodyAsStream() {
        return new ByteArrayInputStream(body);
    }
    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
    public Map<String, String> getQueryParams() { return queryParams; }

    private static Map<String, String> extractParams(String path) {
        Map<String, String> params = new LinkedHashMap<>();
        int q = path.indexOf('?');
        if (q >= 0 && q < path.length() - 1) {
            String[] pairs = path.substring(q + 1).split("&");
            for (String pair : pairs) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    String key = URLDecoder.decode(pair.substring(0, eq),
                            StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(eq + 1),
                            StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}