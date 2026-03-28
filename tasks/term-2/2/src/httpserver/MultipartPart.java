package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MultipartPart {
    private final String name;
    private final String filename;
    private final Map<String, String> headers;
    private final byte[] content;

    MultipartPart(String name, String filename, Map<String, String> headers, byte[] content) {
        this.name = name;
        this.filename = filename;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.content = content.clone();
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getContent() {
        return content.clone();
    }

    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
