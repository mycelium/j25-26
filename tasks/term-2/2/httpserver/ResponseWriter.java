package httpserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ResponseWriter {
    public static void write(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(response.getStatusCode())
                .append(" ")
                .append(response.getStatusText())
                .append("\r\n");
        Map<String, String> headers = response.getHeaders();
        if (!headers.containsKey("content-length") && !headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(response.getBody().length));
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            responseBuilder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        responseBuilder.append("\r\n");
        byte[] headerBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer headerBuffer = ByteBuffer.wrap(headerBytes);
        writeFully(channel, headerBuffer);
        if (response.getBody().length > 0) {
            ByteBuffer bodyBuffer = ByteBuffer.wrap(response.getBody());
            writeFully(channel, bodyBuffer);
        }
    }
    private static void writeFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}