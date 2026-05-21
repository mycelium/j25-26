package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class RequestParser {

    /**
     * Parse raw bytes received from the client.
     *
     * @param data  the raw request bytes
     * @param length number of valid bytes in {@code data}
     * @return parsed {@link HttpRequest}
     * @throws IOException if the request is malformed
     */
    static HttpRequest parse(byte[] data, int length) throws IOException {
        int headerEnd = -1;
        for (int i = 0; i < length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' &&
                data[i + 2] == '\r' && data[i + 3] == '\n') {
                headerEnd = i;
                break;
            }
        }
        if (headerEnd == -1) {
            throw new IOException("Malformed HTTP request: no header/body separator found");
        }

        String headerSection = new String(data, 0, headerEnd);
        String[] lines = headerSection.split("\r\n");

        if (lines.length == 0) {
            throw new IOException("Empty request");
        }

        String[] requestLineParts = lines[0].split(" ", 3);
        if (requestLineParts.length < 3) {
            throw new IOException("Invalid request line: " + lines[0]);
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(requestLineParts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported HTTP method: " + requestLineParts[0]);
        }

        String rawPath = requestLineParts[1];
        String version = requestLineParts[2];

        String path;
        Map<String, String> queryParams = new HashMap<>();
        int qIdx = rawPath.indexOf('?');
        if (qIdx >= 0) {
            path = rawPath.substring(0, qIdx);
            parseQueryString(rawPath.substring(qIdx + 1), queryParams);
        } else {
            path = rawPath;
        }

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                String name = lines[i].substring(0, colon).trim().toLowerCase();
                String value = lines[i].substring(colon + 1).trim();
                headers.put(name, value);
            }
        }

        int bodyStart = headerEnd + 4;
        int bodyLength = length - bodyStart;
        byte[] body = new byte[0];

        if (bodyLength > 0) {
            String cl = headers.get("content-length");
            if (cl != null) {
                try {
                    bodyLength = Math.min(bodyLength, Integer.parseInt(cl.trim()));
                } catch (NumberFormatException ignored) {}
            }
            body = new byte[bodyLength];
            System.arraycopy(data, bodyStart, body, 0, bodyLength);
        }

        return new HttpRequest(method, path, version, headers, body, queryParams);
    }

    private static void parseQueryString(String query, Map<String, String> out) {
        if (query == null || query.isEmpty()) return;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = urlDecode(pair.substring(0, eq));
                String value = urlDecode(pair.substring(eq + 1));
                out.put(key, value);
            } else if (!pair.isEmpty()) {
                out.put(urlDecode(pair), "");
            }
        }
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
