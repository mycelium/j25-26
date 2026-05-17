package http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class RequestParser {

    private static final int BUFFER_SIZE = 8192;
    private static final byte[] HEADER_END = {'\r', '\n', '\r', '\n'};
    private static final byte[] CRLF = {'\r', '\n'};

    private RequestParser() {
    }

    static HttpRequest parse(SocketChannel channel) throws IOException {
        RawRequest rawRequest = readRawRequest(channel);
        String head = new String(rawRequest.head(), StandardCharsets.ISO_8859_1);
        String[] lines = head.split("\r\n");
        if (lines.length == 0 || lines[0].isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] tokens = lines[0].split(" ");
        if (tokens.length < 3) {
            throw new IOException("Malformed start line: " + lines[0]);
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

        Map<String, String> headers = readHeaders(lines);
        int contentLength = contentLength(headers);
        byte[] body = readBody(channel, rawRequest.bodyPrefix(), contentLength);
        Map<String, MultipartPart> parts = parseMultipart(headers, body);

        return new HttpRequest(
                method,
                path,
                version,
                headers,
                parseQuery(rawQuery),
                textFields(parts),
                parts,
                body
        );
    }

    private static RawRequest readRawRequest(SocketChannel channel) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int headerEnd = -1;

        while (headerEnd < 0) {
            buffer.clear();
            int read = channel.read(buffer);
            if (read < 0) {
                throw new IOException("Unexpected end of stream while reading headers");
            }
            if (read == 0) {
                continue;
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                data.write(buffer.get());
            }
            headerEnd = indexOf(data.toByteArray(), HEADER_END, 0);
        }

        byte[] all = data.toByteArray();
        int bodyStart = headerEnd + HEADER_END.length;
        return new RawRequest(
                Arrays.copyOfRange(all, 0, headerEnd),
                Arrays.copyOfRange(all, bodyStart, all.length)
        );
    }

    private static Map<String, String> readHeaders(String[] lines) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String name = lines[i].substring(0, colon).trim().toLowerCase();
            String value = lines[i].substring(colon + 1).trim();
            map.put(name, value);
        }
        return map;
    }

    private static int contentLength(Map<String, String> headers) {
        String value = headers.get("content-length");
        if (value == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static byte[] readBody(SocketChannel channel, byte[] prefix, int length) throws IOException {
        if (length <= 0) {
            return new byte[0];
        }

        ByteArrayOutputStream body = new ByteArrayOutputStream(length);
        body.write(prefix, 0, Math.min(prefix.length, length));
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        while (body.size() < length) {
            buffer.clear();
            int read = channel.read(buffer);
            if (read < 0) {
                throw new IOException("Unexpected end of stream while reading body");
            }
            if (read == 0) {
                continue;
            }

            buffer.flip();
            int needed = length - body.size();
            int count = Math.min(buffer.remaining(), needed);
            byte[] chunk = new byte[count];
            buffer.get(chunk);
            body.write(chunk);
        }

        return body.toByteArray();
    }

    private static Map<String, String> parseQuery(String raw) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
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

    private static Map<String, MultipartPart> parseMultipart(Map<String, String> headers, byte[] body) {
        Map<String, MultipartPart> parts = new LinkedHashMap<>();
        String contentType = headers.get("content-type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return parts;
        }

        String boundary = boundaryOf(contentType);
        if (boundary == null || boundary.isEmpty()) {
            return parts;
        }

        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        int current = indexOf(body, delimiter, 0);
        while (current >= 0) {
            int partStart = current + delimiter.length;
            if (startsWith(body, partStart, "--".getBytes(StandardCharsets.ISO_8859_1))) {
                break;
            }
            if (startsWith(body, partStart, CRLF)) {
                partStart += CRLF.length;
            }

            int next = indexOf(body, delimiter, partStart);
            if (next < 0) {
                break;
            }

            int partEnd = next;
            if (partEnd >= 2 && body[partEnd - 2] == '\r' && body[partEnd - 1] == '\n') {
                partEnd -= 2;
            }

            addPart(parts, body, partStart, partEnd);
            current = next;
        }
        return parts;
    }

    private static String boundaryOf(String contentType) {
        for (String chunk : contentType.split(";")) {
            String token = chunk.trim();
            if (token.startsWith("boundary=")) {
                String boundary = token.substring("boundary=".length());
                if (boundary.startsWith("\"") && boundary.endsWith("\"") && boundary.length() >= 2) {
                    return boundary.substring(1, boundary.length() - 1);
                }
                return boundary;
            }
        }
        return null;
    }

    private static void addPart(Map<String, MultipartPart> parts, byte[] body, int start, int end) {
        int separator = indexOf(body, HEADER_END, start);
        if (separator < 0 || separator >= end) {
            return;
        }

        String rawHeaders = new String(body, start, separator - start, StandardCharsets.ISO_8859_1);
        Map<String, String> headers = readPartHeaders(rawHeaders);
        String disposition = headers.get("content-disposition");
        String name = dispositionValue(disposition, "name");
        if (name == null) {
            return;
        }

        String filename = dispositionValue(disposition, "filename");
        String contentType = headers.get("content-type");
        byte[] content = Arrays.copyOfRange(body, separator + HEADER_END.length, end);
        parts.put(name, new MultipartPart(name, filename, contentType, headers, content));
    }

    private static Map<String, String> readPartHeaders(String rawHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String line : rawHeaders.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            headers.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
        }
        return headers;
    }

    private static String dispositionValue(String disposition, String key) {
        if (disposition == null) {
            return null;
        }
        String prefix = key + "=\"";
        int start = disposition.indexOf(prefix);
        if (start < 0) {
            return null;
        }
        start += prefix.length();
        int end = disposition.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return disposition.substring(start, end);
    }

    private static Map<String, String> textFields(Map<String, MultipartPart> parts) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Map.Entry<String, MultipartPart> entry : parts.entrySet()) {
            MultipartPart part = entry.getValue();
            if (part.filename() == null) {
                fields.put(entry.getKey(), part.bodyAsString());
            }
        }
        return fields;
    }

    private static int indexOf(byte[] data, byte[] pattern, int start) {
        for (int i = Math.max(0, start); i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    private static boolean startsWith(byte[] data, int start, byte[] pattern) {
        if (start < 0 || start + pattern.length > data.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (data[start + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }

    private record RawRequest(byte[] head, byte[] bodyPrefix) {
    }
}
