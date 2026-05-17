package http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

final class RequestParser {

    private static final int CR = '\r';
    private static final int LF = '\n';

    private RequestParser() {
    }

    static HttpRequest parse(InputStream in) throws IOException {
        String startLine = readLine(in);
        if (startLine == null || startLine.isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] tokens = startLine.split(" ");
        if (tokens.length < 3) {
            throw new IOException("Malformed start line: " + startLine);
        }

        HttpMethod method;
        try {
            method = HttpMethod.from(tokens[0]);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Unsupported HTTP method: " + tokens[0], exception);
        }
        String target = tokens[1];
        String version = tokens[2];

        String path;
        String rawQuery;
        int qPos = target.indexOf('?');
        if (qPos < 0) {
            path = target;
            rawQuery = "";
        } else {
            path = target.substring(0, qPos);
            rawQuery = target.substring(qPos + 1);
        }

        Map<String, String> headers = readHeaders(in);

        int contentLength = 0;
        String cl = headers.get("content-length");
        if (cl != null) {
            try {
                contentLength = Integer.parseInt(cl.trim());
            } catch (NumberFormatException nfe) {
                contentLength = 0;
            }
        }

        byte[] body = readBody(in, contentLength);

        Map<String, String> queryParams = parseQuery(rawQuery);
        Map<String, String> formFields = parseMultipart(headers, body);

        return new HttpRequest(method, path, version, headers, queryParams, formFields, body);
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int prev = -1;
        int b;
        while ((b = in.read()) != -1) {
            if (prev == CR && b == LF) {
                byte[] data = buf.toByteArray();
                return new String(data, 0, data.length - 1, StandardCharsets.ISO_8859_1);
            }
            buf.write(b);
            prev = b;
        }
        if (buf.size() == 0) {
            return null;
        }
        return buf.toString(StandardCharsets.ISO_8859_1);
    }

    private static Map<String, String> readHeaders(InputStream in) throws IOException {
        Map<String, String> map = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String name = line.substring(0, colon).trim().toLowerCase();
            String value = line.substring(colon + 1).trim();
            map.put(name, value);
        }
        return map;
    }

    private static byte[] readBody(InputStream in, int length) throws IOException {
        if (length <= 0) {
            return new byte[0];
        }
        byte[] data = new byte[length];
        int total = 0;
        while (total < length) {
            int n = in.read(data, total, length - total);
            if (n < 0) {
                throw new IOException("Unexpected end of stream while reading body");
            }
            total += n;
        }
        return data;
    }

    private static Map<String, String> parseQuery(String raw) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            if (eq < 0) {
                result.put(decode(pair), "");
            } else {
                result.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
            }
        }
        return result;
    }

    private static String decode(String value) throws IOException {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Malformed query parameter", exception);
        }
    }

    private static Map<String, String> parseMultipart(Map<String, String> headers, byte[] body) {
        Map<String, String> fields = new HashMap<>();
        String contentType = headers.get("content-type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return fields;
        }

        String boundary = null;
        for (String chunk : contentType.split(";")) {
            String t = chunk.trim();
            if (t.startsWith("boundary=")) {
                boundary = t.substring("boundary=".length());
                if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                break;
            }
        }
        if (boundary == null) {
            return fields;
        }

        String text = new String(body, StandardCharsets.ISO_8859_1);
        String delimiter = "--" + boundary;
        String[] segments = text.split(java.util.regex.Pattern.quote(delimiter));

        for (String segment : segments) {
            String s = segment;
            if (s.isEmpty() || s.equals("--") || s.equals("--\r\n")) {
                continue;
            }
            if (s.startsWith("\r\n")) {
                s = s.substring(2);
            }

            int sep = s.indexOf("\r\n\r\n");
            if (sep < 0) {
                continue;
            }

            String head = s.substring(0, sep);
            String content = s.substring(sep + 4);
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            }

            String fieldName = extractFieldName(head);
            if (fieldName != null) {
                fields.put(fieldName, content);
            }
        }
        return fields;
    }

    private static String extractFieldName(String headerBlock) {
        for (String line : headerBlock.split("\r\n")) {
            String lower = line.toLowerCase();
            if (!lower.startsWith("content-disposition:")) {
                continue;
            }
            int idx = line.indexOf("name=\"");
            if (idx < 0) {
                return null;
            }
            int start = idx + 6;
            int end = line.indexOf('"', start);
            if (end < 0) {
                return null;
            }
            return line.substring(start, end);
        }
        return null;
    }
}
