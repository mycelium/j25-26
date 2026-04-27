package com.httpserverlib.core;

import com.httpserverlib.model.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseWriter {
    private static final DateTimeFormatter RFC_1123_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O").withZone(ZoneId.of("GMT"));

    public static void write(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatus().getCode())
                .append(" ").append(response.getStatus().getReason()).append("\r\n");
        if (response.getHeader("Date") == null)
            sb.append("Date: ").append(RFC_1123_DATE.format(ZonedDateTime.now())).append("\r\n");
        if (response.getHeader("Server") == null)
            sb.append("Server: Java-HTTP-Library/1.0\r\n");
        byte[] body = response.getBody();
        if (response.getHeader("Content-Length") == null && body.length > 0)
            sb.append("Content-Length: ").append(body.length).append("\r\n");
        for (var entry : response.getHeaders().entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        sb.append("\r\n");
        ByteBuffer headerBuffer = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
        while (headerBuffer.hasRemaining()) channel.write(headerBuffer);
        if (body.length > 0) {
            ByteBuffer bodyBuffer = ByteBuffer.wrap(body);
            while (bodyBuffer.hasRemaining()) channel.write(bodyBuffer);
        }
    }
}