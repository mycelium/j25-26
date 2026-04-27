package com.httpserverlib.core;

import com.httpserverlib.model.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser {

    public static HttpRequest parse(SocketChannel channel) throws IOException {
        LineReader reader = new LineReader(channel);
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;
        String[] parts = requestLine.split(" ");
        if (parts.length < 3) throw new IOException("Invalid request line: " + requestLine);
        HttpMethod method = HttpMethod.fromString(parts[0]);
        if (method == null) throw new IOException("Unsupported method: " + parts[0]);
        String uri = parts[1];
        String path;
        String queryString = null;
        Map<String, String> queryParams = new HashMap<>();
        int queryIdx = uri.indexOf('?');
        if (queryIdx >= 0) {
            path = uri.substring(0, queryIdx);
            queryString = uri.substring(queryIdx + 1);
            parseQueryString(queryString, queryParams);
        } else {
            path = uri;
        }
        if (!path.startsWith("/")) path = "/" + path;
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim();
                String value = line.substring(colonIdx + 1).trim();
                headers.put(name, value);
            }
        }

        // Handle Expect: 100-continue
        String expect = headers.get("Expect");
        if ("100-continue".equalsIgnoreCase(expect)) {
            ByteBuffer continueResponse = ByteBuffer.wrap(
                    "HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.US_ASCII)
            );
            while (continueResponse.hasRemaining()) {
                channel.write(continueResponse);
            }
        }


        byte[] body = new byte[0];
        List<Part> multipartParts = null;

        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength > 0) {
                body = reader.readRawBytes(contentLength);
            }
        }
        String contentType = headers.get("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) multipartParts =
                    com.httpserverlib.multipart.MultipartParser.parse(body, boundary);
        }
        return new HttpRequest.Builder()
                .method(method).path(path).queryString(queryString).queryParams(queryParams)
                .headers(headers).body(body).multipartParts(multipartParts).build();
    }

    private static byte[] readExactly(SocketChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int totalRead = 0;
        while (totalRead < length) {
            int read = channel.read(buffer);
            if (read == -1) throw new IOException("Unexpected end of stream");
            totalRead += read;
        }
        buffer.flip();
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    private static void parseQueryString(String query, Map<String, String> params) {
        if (query == null || query.isEmpty()) return;
        for (String pair : query.split("&")) {
            int eqIdx = pair.indexOf('=');
            if (eqIdx > 0) {
                String key = URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
    }

    private static String extractBoundary(String contentType) {
        String boundaryParam = "boundary=";
        int idx = contentType.indexOf(boundaryParam);
        if (idx >= 0) {
            String boundary = contentType.substring(idx + boundaryParam.length());
            if (boundary.startsWith("\"") && boundary.endsWith("\""))
                boundary = boundary.substring(1, boundary.length() - 1);
            return "--" + boundary;
        }
        return null;
    }
}