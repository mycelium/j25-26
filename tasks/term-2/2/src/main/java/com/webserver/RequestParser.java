package com.webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.Map;

public class RequestParser {

    public static Request parse(InputStream in) throws IOException {
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isBlank()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return null;

        String method = parts[0];
        String path = parts[1];

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.put(name, value);
            }
        }

        byte[] body = new byte[0];
        String contentLength = headers.get("Content-Length");
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength.trim());
            if (length > 0) {
                body = in.readNBytes(length);
            }
        }

        return new Request(method, path, headers, body);
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int prev = -1, cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') {
                byte[] data = buffer.toByteArray();
                return new String(data, 0, data.length - 1, StandardCharsets.ISO_8859_1);
            }
            buffer.write(cur);
            prev = cur;
        }
        if (buffer.size() == 0) return null;
        return buffer.toString(StandardCharsets.ISO_8859_1);
    }
}
