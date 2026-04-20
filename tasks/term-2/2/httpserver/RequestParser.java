package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser {

    public static HttpRequest parse(SocketChannel channel) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ByteBuffer temp = ByteBuffer.allocate(8192);
        int headerEnd = -1;

        while (headerEnd == -1) {
            int read = channel.read(temp);
            if (read == -1) throw new IOException("Connection closed while reading headers");
            temp.flip();
            byte[] bytes = new byte[temp.remaining()];
            temp.get(bytes);
            buffer.write(bytes);
            temp.clear();
            headerEnd = findHeaderEnd(buffer.toByteArray());
        }

        byte[] allBytes = buffer.toByteArray();
        byte[] headerBytes = Arrays.copyOfRange(allBytes, 0, headerEnd);
        String headerStr = new String(headerBytes, StandardCharsets.UTF_8);

        String[] lines = headerStr.split("\r\n");
        if (lines.length < 1) throw new IOException("Invalid HTTP request");

        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 3) throw new IOException("Invalid request line");

        HttpMethod method = HttpMethod.valueOf(requestLine[0]);
        String rawPath = requestLine[1];
        String version = requestLine[2];

        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
        }

        int contentLength = 0;
        List<String> clHeader = headers.get("Content-Length");
        if (clHeader != null && !clHeader.isEmpty()) {
            try { contentLength = Integer.parseInt(clHeader.get(0)); } catch (NumberFormatException ignored) {}
        }

        byte[] body;
        int bodyStart = headerEnd;
        int alreadyRead = allBytes.length - bodyStart;

        if (alreadyRead >= contentLength) {
            body = Arrays.copyOfRange(allBytes, bodyStart, bodyStart + contentLength);
        } else {
            ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
            bodyBuffer.write(allBytes, bodyStart, alreadyRead);
            int remaining = contentLength - alreadyRead;
            while (remaining > 0) {
                int read = channel.read(temp);
                if (read == -1) break;
                temp.flip();
                int toRead = Math.min(temp.remaining(), remaining);
                byte[] b = new byte[toRead];
                temp.get(b);
                bodyBuffer.write(b);
                remaining -= toRead;
                temp.clear();
            }
            body = bodyBuffer.toByteArray();
        }

        String path = rawPath;
        Map<String, List<String>> queryParams = new HashMap<>();
        int qIdx = rawPath.indexOf('?');
        if (qIdx != -1) {
            path = rawPath.substring(0, qIdx);
            String query = rawPath.substring(qIdx + 1);
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length > 0) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                    queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                }
            }
        }

        return new HttpRequest(method, path, rawPath, version, headers, body, queryParams);
    }

    private static int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i+1] == '\n' && data[i+2] == '\r' && data[i+3] == '\n') {
                return i + 4;
            }
        }
        return -1;
    }
}