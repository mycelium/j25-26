package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

class RequestParser {

    static HttpRequest parse(SocketChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8192);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int headerEnd = -1;
        while (headerEnd < 0) {
            int n = channel.read(buf);
            if (n == -1) return null;
            buf.flip();
            byte[] chunk = new byte[buf.remaining()];
            buf.get(chunk);
            out.write(chunk);
            buf.clear();
            headerEnd = findSequence(out.toByteArray());
        }

        byte[] all = out.toByteArray();
        String headerBlock = new String(all, 0, headerEnd, StandardCharsets.UTF_8);
        String[] lines = headerBlock.split("\r\n");
        String[] requestLine = lines[0].split(" ", 3);

        HttpMethod method = HttpMethod.valueOf(requestLine[0]);
        String rawPath = requestLine[1];

        String path = rawPath;
        Map<String, String> queryParams = new LinkedHashMap<>();
        int q = rawPath.indexOf('?');
        if (q >= 0) {
            path = rawPath.substring(0, q);
            for (String param : rawPath.substring(q + 1).split("&")) {
                String[] kv = param.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }

        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                String key = lines[i].substring(0, colon).trim().toLowerCase();
                String value = lines[i].substring(colon + 1).trim();
                headers.put(key, value);
            }
        }

        byte[] body = new byte[0];
        String cl = headers.get("content-length");
        if (cl != null) {
            int contentLength = Integer.parseInt(cl.trim());
            int bodyStart = headerEnd + 4;
            while (out.size() - bodyStart < contentLength) {
                int n = channel.read(buf);
                if (n == -1) break;
                buf.flip();
                byte[] chunk = new byte[buf.remaining()];
                buf.get(chunk);
                out.write(chunk);
                buf.clear();
            }
            all = out.toByteArray();
            body = new byte[contentLength];
            System.arraycopy(all, bodyStart, body, 0, Math.min(contentLength, all.length - bodyStart));
        }

        return new HttpRequest(method, path, queryParams, headers, body);
    }

    private static int findSequence(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
}
