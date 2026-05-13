package org.internal;

import org.api.MultipartPart;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MultipartParser {

    private MultipartParser() {}

    public static List<MultipartPart> parse(byte[] body, String contentType) {
        String boundary = extractBoundary(contentType);
        if (boundary == null) return List.of();

        byte[] delimiter  = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        List<MultipartPart> parts = new ArrayList<>();

        int pos = 0;
        while (pos < body.length) {
            int start = indexOf(body, delimiter, pos);
            if (start == -1) break;
            start += delimiter.length;

            if (start + 2 <= body.length && body[start] == '-' && body[start + 1] == '-') break;
            if (start + 2 <= body.length && body[start] == '\r' && body[start + 1] == '\n') start += 2;

            int nextBoundary = indexOf(body, delimiter, start);
            if (nextBoundary == -1) break;

            int partEnd = nextBoundary;
            if (partEnd >= 2 && body[partEnd - 2] == '\r' && body[partEnd - 1] == '\n') partEnd -= 2;

            int headerEnd = indexOf(body, new byte[]{'\r', '\n', '\r', '\n'}, start);
            if (headerEnd == -1 || headerEnd >= partEnd) { pos = nextBoundary; continue; }

            byte[] rawHeaders = copyRange(body, start, headerEnd);
            byte[] partBody   = copyRange(body, headerEnd + 4, partEnd);

            Map<String, String> headerMap = parseHeaders(new String(rawHeaders, StandardCharsets.UTF_8));
            parts.add(new MultipartPart(headerMap, partBody));

            pos = nextBoundary;
        }
        return parts;
    }

    private static String extractBoundary(String contentType) {
        if (contentType == null) return null;
        for (String token : contentType.split(";")) {
            token = token.trim();
            if (token.startsWith("boundary="))
                return token.substring("boundary=".length()).replace("\"", "").trim();
        }
        return null;
    }

    private static Map<String, String> parseHeaders(String raw) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : raw.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon == -1) continue;
            map.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
        }
        return map;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from) {
        outer:
        for (int i = from; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static byte[] copyRange(byte[] src, int from, int to) {
        if (to <= from) return new byte[0];
        byte[] result = new byte[to - from];
        System.arraycopy(src, from, result, 0, result.length);
        return result;
    }
}
