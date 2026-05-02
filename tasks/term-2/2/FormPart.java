import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class FormPart {
    private final Map<String, String> headers;
    private final byte[] content;

    public FormPart(Map<String, String> headers, byte[] content) {
        this.headers = new HashMap<>(headers);
        this.content = content;
    }

    public String getName() {
        return extractParameter("name");
    }

    public String getFileName() {
        return extractParameter("filename");
    }

    public String getValue() {
        return new String(content, StandardCharsets.UTF_8);
    }

    public byte[] getContent() {
        return content;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    private String extractParameter(String paramName) {
        String disposition = headers.get("Content-Disposition");
        if (disposition == null) return null;
        
        for (String part : disposition.split(";")) {
            part = part.trim();
            if (part.startsWith(paramName + "=")) {
                String value = part.substring(paramName.length() + 1);
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }
}
