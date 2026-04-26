package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;

public class FormPart {
    private final Map<String, String> disposition;
    private final Map<String, String> headers;
    private final byte[] content;
    
    public FormPart(Map<String, String> headers, byte[] content) {
        this.headers = headers;
        this.content = content;
        String cd = headers.get("content-disposition");
        this.disposition = parseDisposition(cd);
    }
    
    private Map<String, String> parseDisposition(String cd) {
        Map<String, String> result = new LinkedHashMap<>();
        if (cd == null) return result;
        String[] parts = cd.split(";");
        for (String part : parts) {
            part = part.trim();
            int eqIdx = part.indexOf('=');
            if (eqIdx != -1) {
                String key = part.substring(0, eqIdx);
                String val = part.substring(eqIdx + 1);
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                result.put(key, val);
            } else {
                result.put("type", part);
            }
        }
        return result;
    }
    
    public String getName() { return disposition.get("name"); }
    public String getFilename() { return disposition.get("filename"); }
    public String getText() { return new String(content, StandardCharsets.UTF_8); }
    public byte[] getBytes() { return content; }
    public boolean isFile() { return getFilename() != null; }
}
