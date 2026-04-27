package com.httpserverlib.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

public class LineReader {
    private final SocketChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocate(8192);
    private final Queue<Byte> queue = new ArrayDeque<>();
    private boolean lastCharCR = false;

    public LineReader(SocketChannel channel) {
        this.channel = channel;
    }

    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (queue.isEmpty()) {
                buffer.clear();
                int read = channel.read(buffer);
                if (read == -1) return null;
                buffer.flip();
                while (buffer.hasRemaining()) queue.add(buffer.get());
            }
            while (!queue.isEmpty()) {
                byte b = queue.poll();
                if (lastCharCR && b == '\n') {
                    lastCharCR = false;
                    return sb.toString();
                }
                if (b == '\r') {
                    lastCharCR = true;
                    continue;
                }
                if (lastCharCR) {
                    sb.append((char) '\r');
                    lastCharCR = false;
                }
                sb.append((char) b);
            }
        }
    }

    // NEW: read exactly 'length' bytes (using queue first, then channel)
    public byte[] readRawBytes(int length) throws IOException {
        byte[] result = new byte[length];
        int offset = 0;
        // take from queue first
        while (offset < length && !queue.isEmpty()) {
            result[offset++] = queue.poll();
        }
        // then read directly from channel
        if (offset < length) {
            ByteBuffer buf = ByteBuffer.allocate(length - offset);
            int totalRead = 0;
            while (totalRead < buf.capacity()) {
                int read = channel.read(buf);
                if (read == -1) throw new IOException("Unexpected EOF while reading body");
                totalRead += read;
            }
            buf.flip();
            buf.get(result, offset, buf.remaining());
        }
        return result;
    }
}