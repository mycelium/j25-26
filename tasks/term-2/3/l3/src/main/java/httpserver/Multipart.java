package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Multipart {
    private Multipart() {}

    public static Result parse(byte[] body, String contentType) {
        Map<String, String> fields = new HashMap<>();
        Map<String, byte[]> files = new HashMap<>();

        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return new Result(fields, files);
        }

        String boundary = null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring(9).replace("\"", "");
                break;
            }
        }
        if (boundary == null) return new Result(fields, files);

        String text = new String(body, StandardCharsets.UTF_8);
        String[] parts = text.split("--" + boundary + "(--)?\r\n");

        for (String part : parts) {
            if (part.isBlank()) continue;
            int split = part.indexOf("\r\n\r\n");
            if (split == -1) continue;

            String head = part.substring(0, split);
            String content = part.substring(split + 4);
            if (content.endsWith("\r\n")) content = content.substring(0, content.length() - 2);

            String name = null, filename = null;
            for (String line : head.split("\r\n")) {
                if (line.toLowerCase().contains("content-disposition")) {
                    if (line.contains("name=\"")) {
                        int n1 = line.indexOf("name=\"") + 6;
                        int n2 = line.indexOf('"', n1);
                        name = line.substring(n1, n2);
                    }
                    if (line.contains("filename=\"")) {
                        int f1 = line.indexOf("filename=\"") + 10;
                        int f2 = line.indexOf('"', f1);
                        filename = line.substring(f1, f2);
                    }
                }
            }

            if (name != null) {
                if (filename != null && !filename.isEmpty()) {
                    files.put(name, content.getBytes(StandardCharsets.UTF_8));
                } else {
                    fields.put(name, content);
                }
            }
        }
        return new Result(fields, files);
    }

    public record Result(Map<String, String> fields, Map<String, byte[]> files) {}
}