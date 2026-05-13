package org.internal;

import org.api.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public final class HttpResponseWriter {

    private HttpResponseWriter() {}

    public static void write(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ")
          .append(response.statusCode()).append(' ')
          .append(response.statusText()).append("\r\n");

        byte[] body = response.body();
        response.headers().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("Content-Length: ").append(body.length).append("\r\n");
        sb.append("Connection: close\r\n");
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] full        = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(body,        0, full, headerBytes.length, body.length);

        ByteBuffer buf = ByteBuffer.wrap(full);
        while (buf.hasRemaining()) channel.write(buf);
    }
}
