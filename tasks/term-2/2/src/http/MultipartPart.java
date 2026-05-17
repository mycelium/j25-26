package http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MultipartPart {

    private final String name;
    private final String filename;
    private final String contentType;
    private final Map<String, String> headers;
    private final byte[] body;

    public MultipartPart(String name,
                         String filename,
                         String contentType,
                         Map<String, String> headers,
                         byte[] body) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    public String name() {
        return name;
    }

    public String filename() {
        return filename;
    }

    public String contentType() {
        return contentType;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        return body.clone();
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
