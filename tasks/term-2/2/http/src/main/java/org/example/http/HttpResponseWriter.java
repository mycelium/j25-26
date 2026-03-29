package org.example.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseWriter {

    private static final String HTTP_VERSION = "HTTP/1.1";

    private HttpResponseWriter() {}

    public static void write(SocketChannel channel, HttpResponse response) throws IOException {
        byte[] bodyBytes = toBytes(response.getBody());

        StringBuilder headerBuilder = new StringBuilder(256);
        headerBuilder
                .append(HTTP_VERSION).append(' ')
                .append(response.getStatusCode()).append(' ')
                .append(response.getReasonPhrase())
                .append("\r\n");

        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            headerBuilder
                    .append(entry.getKey()).append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }

        headerBuilder.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        headerBuilder.append("Connection: close\r\n");
        headerBuilder.append("\r\n");

        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        buffer.put(headerBytes);
        buffer.put(bodyBytes);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static byte[] toBytes(String s) {
        return s != null ? s.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }
}