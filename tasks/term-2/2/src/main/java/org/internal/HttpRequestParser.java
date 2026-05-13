package org.internal;

import org.api.HttpMethod;
import org.api.HttpRequest;
import org.api.MultipartPart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HttpRequestParser {

    private static final int    BUFFER_SIZE = 8192;
    private static final byte[] HEADER_END  = {'\r', '\n', '\r', '\n'};

    private HttpRequestParser() {}

    public static HttpRequest parse(SocketChannel channel) throws IOException {
        byte[] raw = readUntilHeaderEnd(channel);
        if (raw == null || raw.length == 0) return null;

        int headerEndIdx = indexOf(raw, HEADER_END, 0);
        if (headerEndIdx == -1) return null;

        String   headerSection = new String(copyRange(raw, 0, headerEndIdx), StandardCharsets.UTF_8);
        String[] lines         = headerSection.split("\r\n");
        if (lines.length == 0) return null;

        String[] requestLine = lines[0].split(" ", 3);
        if (requestLine.length < 2) return null;

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(requestLine[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        String fullPath    = requestLine[1];
        int    qmark       = fullPath.indexOf('?');
        String path        = qmark == -1 ? fullPath : fullPath.substring(0, qmark);
        String queryString = qmark == -1 ? ""       : fullPath.substring(qmark + 1);

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon == -1) continue;
            headers.put(lines[i].substring(0, colon).trim().toLowerCase(),
                        lines[i].substring(colon + 1).trim());
        }

        Map<String, String> queryParams = parseQueryString(queryString);

        int contentLength = 0;
        if (headers.containsKey("content-length")) {
            try { contentLength = Integer.parseInt(headers.get("content-length")); }
            catch (NumberFormatException ignored) {}
        }

        byte[] bodyPrefix = copyRange(raw, headerEndIdx + HEADER_END.length, raw.length);
        byte[] body       = readBody(channel, bodyPrefix, contentLength);

        String              contentType = headers.getOrDefault("content-type", "");
        List<MultipartPart> parts       = contentType.startsWith("multipart/form-data")
                ? MultipartParser.parse(body, contentType)
                : List.of();

        return new HttpRequest(method, path, queryString, headers, queryParams, body, parts);
    }

    private static byte[] readUntilHeaderEnd(SocketChannel channel) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ByteBuffer            bb  = ByteBuffer.allocate(BUFFER_SIZE);

        while (true) {
            bb.clear();
            int n = channel.read(bb);
            if (n == -1) break;
            bb.flip();
            byte[] chunk = new byte[bb.remaining()];
            bb.get(chunk);
            buf.write(chunk);
            if (indexOf(buf.toByteArray(), HEADER_END, 0) != -1) break;
        }
        return buf.toByteArray();
    }

    private static byte[] readBody(SocketChannel channel, byte[] prefix, int contentLength) throws IOException {
        if (contentLength <= 0) return new byte[0];

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(prefix);

        ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
        while (buf.size() < contentLength) {
            bb.clear();
            int n = channel.read(bb);
            if (n == -1) break;
            bb.flip();
            byte[] chunk = new byte[bb.remaining()];
            bb.get(chunk);
            buf.write(chunk);
        }

        byte[] all = buf.toByteArray();
        if (all.length > contentLength) {
            byte[] trimmed = new byte[contentLength];
            System.arraycopy(all, 0, trimmed, 0, contentLength);
            return trimmed;
        }
        return all;
    }

    private static Map<String, String> parseQueryString(String qs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (qs == null || qs.isEmpty()) return map;
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            if (eq == -1) map.put(decode(pair), "");
            else          map.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
        }
        return map;
    }

    private static String decode(String s) {
        try { return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
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
