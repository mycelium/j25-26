package httpserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class HttpRequestParser {
    private HttpRequestParser() {
    }

    static ParsedRequest parse(SocketChannel socketChannel) throws IOException, HttpParseException {
        InputStream rawInput = Channels.newInputStream(socketChannel);
        BufferedInputStream inputStream = new BufferedInputStream(rawInput);

        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isBlank()) {
            throw new HttpParseException("Missing request line");
        }

        String[] requestParts = requestLine.split(" ", 3);
        if (requestParts.length != 3) {
            throw new HttpParseException("Invalid request line");
        }

        String methodToken = requestParts[0];
        String target = requestParts[1];
        String version = requestParts[2];

        if (!"HTTP/1.1".equals(version)) {
            throw new HttpParseException("Unsupported HTTP version");
        }

        if (!target.startsWith("/")) {
            throw new HttpParseException("Invalid request target");
        }

        Map<String, String> headers = readHeaders(inputStream);
        int contentLength = parseContentLength(headers.get("content-length"));
        byte[] body = readBody(inputStream, contentLength);

        String path = target;
        Map<String, String> queryParams = Collections.emptyMap();
        int querySeparator = target.indexOf('?');
        if (querySeparator >= 0) {
            path = target.substring(0, querySeparator);
            queryParams = parseQueryParams(target.substring(querySeparator + 1));
        }

        HttpMethod method = HttpMethod.fromToken(methodToken);
        List<MultipartPart> multipartParts = List.of();
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
            multipartParts = MultipartParser.parse(body, contentType);
        }

        return new ParsedRequest(methodToken, method, path, version, headers, queryParams, body, multipartParts);
    }

    private static Map<String, String> readHeaders(InputStream inputStream) throws IOException, HttpParseException {
        Map<String, String> headers = new LinkedHashMap<>();
        while (true) {
            String line = readLine(inputStream);
            if (line == null) {
                throw new HttpParseException("Unexpected end of headers");
            }
            if (line.isEmpty()) {
                return headers;
            }

            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                throw new HttpParseException("Invalid header line");
            }

            String name = line.substring(0, colonIndex).trim().toLowerCase();
            String value = line.substring(colonIndex + 1).trim();
            headers.put(name, value);
        }
    }

    private static int parseContentLength(String contentLengthHeader) throws HttpParseException {
        if (contentLengthHeader == null || contentLengthHeader.isBlank()) {
            return 0;
        }
        try {
            int contentLength = Integer.parseInt(contentLengthHeader.trim());
            if (contentLength < 0) {
                throw new HttpParseException("Negative content length");
            }
            return contentLength;
        } catch (NumberFormatException exception) {
            throw new HttpParseException("Invalid content length", exception);
        }
    }

    private static byte[] readBody(InputStream inputStream, int contentLength) throws IOException, HttpParseException {
        if (contentLength == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(contentLength);
        byte[] buffer = new byte[4096];
        int remaining = contentLength;

        while (remaining > 0) {
            int read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining));
            if (read == -1) {
                throw new HttpParseException("Unexpected end of body");
            }
            outputStream.write(buffer, 0, read);
            remaining -= read;
        }

        return outputStream.toByteArray();
    }

    private static String readLine(InputStream inputStream) throws IOException, HttpParseException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = -1;

        while (true) {
            int current = inputStream.read();
            if (current == -1) {
                if (buffer.size() == 0) {
                    return null;
                }
                throw new HttpParseException("Unexpected end of stream");
            }

            if (previous == '\r' && current == '\n') {
                byte[] bytes = buffer.toByteArray();
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.ISO_8859_1);
            }

            buffer.write(current);
            previous = current;
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (query.isBlank()) {
            return queryParams;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }

            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            queryParams.put(decodeComponent(key), decodeComponent(value));
        }

        return queryParams;
    }

    private static String decodeComponent(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    static final class ParsedRequest {
        private final String methodToken;
        private final HttpMethod method;
        private final String path;
        private final String version;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;
        private final byte[] body;
        private final List<MultipartPart> multipartParts;

        ParsedRequest(
                String methodToken,
                HttpMethod method,
                String path,
                String version,
                Map<String, String> headers,
                Map<String, String> queryParams,
                byte[] body,
                List<MultipartPart> multipartParts
        ) {
            this.methodToken = methodToken;
            this.method = method;
            this.path = path;
            this.version = version;
            this.headers = headers;
            this.queryParams = queryParams;
            this.body = body;
            this.multipartParts = multipartParts;
        }

        String methodToken() {
            return methodToken;
        }

        HttpMethod method() {
            return method;
        }

        String path() {
            return path;
        }

        String version() {
            return version;
        }

        Map<String, String> headers() {
            return headers;
        }

        Map<String, String> queryParams() {
            return queryParams;
        }

        byte[] body() {
            return body;
        }

        List<MultipartPart> multipartParts() {
            return multipartParts;
        }
    }

    static final class HttpParseException extends Exception {
        HttpParseException(String message) {
            super(message);
        }

        HttpParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
