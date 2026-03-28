package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MultipartParser {
    private MultipartParser() {
    }

    static List<MultipartPart> parse(byte[] body, String contentType) throws HttpRequestParser.HttpParseException {
        String boundary = extractBoundary(contentType);
        if (boundary == null || boundary.isBlank()) {
            throw new HttpRequestParser.HttpParseException("Missing multipart boundary");
        }

        String raw = new String(body, StandardCharsets.ISO_8859_1);
        String delimiter = "--" + boundary;
        List<MultipartPart> parts = new ArrayList<>();
        int cursor = 0;

        if (!raw.startsWith(delimiter)) {
            throw new HttpRequestParser.HttpParseException("Invalid multipart payload");
        }

        while (cursor < raw.length()) {
            if (!raw.startsWith(delimiter, cursor)) {
                throw new HttpRequestParser.HttpParseException("Invalid multipart delimiter");
            }

            cursor += delimiter.length();
            if (cursor + 2 <= raw.length() && raw.startsWith("--", cursor)) {
                break;
            }

            if (cursor + 2 > raw.length() || !raw.startsWith("\r\n", cursor)) {
                throw new HttpRequestParser.HttpParseException("Invalid multipart separator");
            }
            cursor += 2;

            int headersEnd = raw.indexOf("\r\n\r\n", cursor);
            if (headersEnd < 0) {
                throw new HttpRequestParser.HttpParseException("Missing multipart headers");
            }

            String headerBlock = raw.substring(cursor, headersEnd);
            Map<String, String> partHeaders = parseHeaders(headerBlock);
            cursor = headersEnd + 4;

            int nextDelimiter = raw.indexOf("\r\n" + delimiter, cursor);
            if (nextDelimiter < 0) {
                throw new HttpRequestParser.HttpParseException("Unterminated multipart part");
            }

            byte[] content = raw.substring(cursor, nextDelimiter).getBytes(StandardCharsets.ISO_8859_1);
            cursor = nextDelimiter + 2;

            String disposition = partHeaders.get("content-disposition");
            String name = extractDispositionValue(disposition, "name");
            String filename = extractDispositionValue(disposition, "filename");

            parts.add(new MultipartPart(name, filename, partHeaders, content));

            if (raw.startsWith(delimiter + "--", cursor)) {
                break;
            }
        }

        return parts;
    }

    private static Map<String, String> parseHeaders(String headerBlock) throws HttpRequestParser.HttpParseException {
        Map<String, String> headers = new LinkedHashMap<>();
        String[] lines = headerBlock.split("\r\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                throw new HttpRequestParser.HttpParseException("Invalid multipart header");
            }
            String name = line.substring(0, colonIndex).trim().toLowerCase();
            String value = line.substring(colonIndex + 1).trim();
            headers.put(name, value);
        }
        return headers;
    }

    private static String extractBoundary(String contentType) {
        for (String part : contentType.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("boundary=")) {
                String boundary = trimmed.substring("boundary=".length()).trim();
                return stripQuotes(boundary);
            }
        }
        return null;
    }

    private static String extractDispositionValue(String disposition, String key) {
        if (disposition == null) {
            return null;
        }

        for (String part : disposition.split(";")) {
            String trimmed = part.trim();
            String prefix = key + "=";
            if (trimmed.startsWith(prefix)) {
                return stripQuotes(trimmed.substring(prefix.length()));
            }
        }

        return null;
    }

    private static String stripQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
