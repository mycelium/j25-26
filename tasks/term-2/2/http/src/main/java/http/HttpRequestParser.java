package http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestParser {

    private static final int BUFFER_SIZE = 8192;
    private static final byte[] HEADER_TERMINATOR = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

    private HttpRequestParser() {}

    public static HttpRequest parse(SocketChannel channel) throws IOException {

        ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
        byte[] accumulated = new byte[0];
        int headerEndIndex = -1;

        while (headerEndIndex < 0) {
            readBuf.clear();
            int n = channel.read(readBuf);
            if (n == -1) break;
            if (n == 0) continue;
            readBuf.flip();
            byte[] chunk = new byte[readBuf.remaining()];
            readBuf.get(chunk);
            accumulated = concat(accumulated, chunk);
            headerEndIndex = indexOf(accumulated, HEADER_TERMINATOR);
        }

        if (headerEndIndex < 0) {
            if (accumulated.length == 0) {
                throw new EmptyRequestException();
            }
            throw new IOException("Malformed HTTP request: header terminator not found");
        }

        String headerSection = new String(accumulated, 0, headerEndIndex, StandardCharsets.UTF_8);
        int bodyOffset = headerEndIndex + HEADER_TERMINATOR.length;
        byte[] earlyBody = new byte[accumulated.length - bodyOffset];
        System.arraycopy(accumulated, bodyOffset, earlyBody, 0, earlyBody.length);

        String[] lines = headerSection.split("\r\n", -1);
        if (lines.length == 0 || lines[0].isBlank()) {
            throw new IOException("Malformed HTTP request: empty request line");
        }

        String[] requestLineParts = lines[0].split(" ", 3);
        if (requestLineParts.length < 3) {
            throw new IOException("Malformed HTTP request line: " + lines[0]);
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(requestLineParts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IOException("Unsupported HTTP method: " + requestLineParts[0]);
        }

        String fullPath   = requestLineParts[1];
        String httpVersion = requestLineParts[2];

        String path;
        Map<String, String> queryParams = new LinkedHashMap<>();
        int qMark = fullPath.indexOf('?');
        if (qMark >= 0) {
            path = fullPath.substring(0, qMark);
            parseQueryString(fullPath.substring(qMark + 1), queryParams);
        } else {
            path = fullPath;
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                String key   = lines[i].substring(0, colon).trim();
                String value = lines[i].substring(colon + 1).trim();
                headers.put(key, value);
            }
        }

        String body = "";
        String contentLengthHeader = headers.get("Content-Length");
        if (contentLengthHeader != null) {
            int contentLength;
            try {
                contentLength = Integer.parseInt(contentLengthHeader.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Invalid Content-Length value: " + contentLengthHeader);
            }

            byte[] bodyBytes  = new byte[contentLength];
            int    alreadyHave = Math.min(earlyBody.length, contentLength);
            System.arraycopy(earlyBody, 0, bodyBytes, 0, alreadyHave);

            int offset    = alreadyHave;
            int remaining = contentLength - alreadyHave;

            while (remaining > 0) {
                readBuf.clear();
                int n = channel.read(readBuf);
                if (n == -1) break;
                readBuf.flip();
                int toRead = Math.min(readBuf.remaining(), remaining);
                readBuf.get(bodyBytes, offset, toRead);
                offset    += toRead;
                remaining -= toRead;
            }

            body = new String(bodyBytes, StandardCharsets.UTF_8);
        }

        return new HttpRequest(method, path, httpVersion, headers, body, queryParams);
    }

    private static int indexOf(byte[] data, byte[] pattern) {
        outer:
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static void parseQueryString(String queryString, Map<String, String> params) {
        for (String pair : queryString.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(pair.substring(0, eq), pair.substring(eq + 1));
            } else {
                params.put(pair, "");
            }
        }
    }
}