package org.example.http;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestParser {
    public HttpRequest parse(SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();

        boolean headersEnd = false;
        byte[] last4Bytes = new byte[4];

        while (!headersEnd) {
            int read = channel.read(buffer);
            if (read == -1) return null;

            buffer.flip();
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                headerStream.write(b);

                System.arraycopy(last4Bytes, 1, last4Bytes, 0, 3);
                last4Bytes[3] = b;

                if (last4Bytes[0] == '\r' && last4Bytes[1] == '\n' &&
                    last4Bytes[2] == '\r' && last4Bytes[3] == '\n') {
                    headersEnd = true;
                    break;
                }
            }
            buffer.clear();
        }

        String headerText = new String(headerStream.toByteArray(), "UTF-8");
        String[] lines = headerText.split("\r\n");

        if (lines.length == 0) throw new IllegalArgumentException("Empty request");

        String[] requestLine = lines[0].split("\\s+");
        if (requestLine.length < 3) throw new IllegalArgumentException("Invalid request line");

        HttpMethod method = HttpMethod.fromString(requestLine[0]);
        String fullPath = requestLine[1];
        String protocol = requestLine[2];

        Map<String, String> queryParams = new HashMap<>();
        String path = fullPath;
        if (fullPath.contains("?")) {
            String[] parts = fullPath.split("\\?", 2);
            path = parts[0];
            String query = parts[1];
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) queryParams.put(kv[0], kv[1]);
                else if (kv.length == 1) queryParams.put(kv[0], "");
            }
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) continue;
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        byte[] body = new byte[0];
        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr.trim());
                if (contentLength > 0) {
                    body = readBody(channel, contentLength);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid Content-Length");
            }
        }

        return new HttpRequest(method, path, protocol, headers, body, queryParams);
    }

    private byte[] readBody(SocketChannel channel, int length) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int totalRead = 0;

        while (totalRead < length) {
            int remaining = length - totalRead;
            buffer.clear();
            buffer.limit(Math.min(remaining, buffer.capacity()));

            int read = channel.read(buffer);
            if (read == -1) break;

            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            baos.write(bytes);

            totalRead += bytes.length;
        }
        return baos.toByteArray();
    }
}