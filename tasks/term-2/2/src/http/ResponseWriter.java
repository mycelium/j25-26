package http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class ResponseWriter {

    private ResponseWriter() {
    }

    static void write(SocketChannel channel, HttpResponse response) throws IOException {
        byte[] body = response.body();
        response.mutableHeaders().putIfAbsent("Content-Length", String.valueOf(body.length));

        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ")
            .append(response.statusCode())
            .append(' ')
            .append(reasonOf(response.statusCode()))
            .append("\r\n");

        for (Map.Entry<String, String> h : response.headers().entrySet()) {
            head.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        head.append("\r\n");

        writeFully(channel, ByteBuffer.wrap(head.toString().getBytes(StandardCharsets.UTF_8)));
        if (body.length > 0) {
            writeFully(channel, ByteBuffer.wrap(body));
        }
    }

    private static void writeFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static String reasonOf(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "OK";
        };
    }
}
