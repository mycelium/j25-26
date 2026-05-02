package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Reads raw bytes from a {@link SocketChannel} and dispatches the accepted
 * client to the correct {@link HttpServer.Handler}.
 *
 * <p>Parsing logic itself lives in {@link HttpRequest#parse(byte[])};
 * this class is responsible only for I/O and routing.
 */
class HttpRequestParser implements Runnable {

    private static final int BUFFER_SIZE = 8192;

    private final SocketChannel channel;
    private final java.util.Map<RouteKey, HttpServer.Handler> routes;

    HttpRequestParser(SocketChannel channel, java.util.Map<RouteKey, HttpServer.Handler> routes) {
        this.channel = channel;
        this.routes  = routes;
    }

    @Override
    public void run() {
        try (channel) {
            byte[] raw      = readAll(channel);
            HttpRequest req = HttpRequest.parse(raw);
            HttpResponse res = dispatch(req);
            send(res);
        } catch (Exception e) {
            try { send(error500(e)); } catch (IOException ignored) {}
        }
    }

    // ── Reading ────────────────────────────────────────────────────────────────

    private byte[] readAll(SocketChannel ch) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        int headerEnd = -1, contentLength = -1;

        while (true) {
            buf.clear();
            int n = ch.read(buf);
            if (n == -1) break;
            buf.flip();
            byte[] chunk = new byte[buf.limit()];
            buf.get(chunk);
            out.write(chunk);

            byte[] so = out.toByteArray();
            if (headerEnd == -1) {
                headerEnd = HttpRequest.indexOf(so, "\r\n\r\n".getBytes(StandardCharsets.UTF_8), 0);
                if (headerEnd != -1) {
                    contentLength = contentLength(new String(so, 0, headerEnd, StandardCharsets.UTF_8));
                }
            }
            if (headerEnd != -1) {
                int received = so.length - headerEnd - 4;
                if (contentLength <= 0 || received >= contentLength) break;
            }
        }
        return out.toByteArray();
    }

    private int contentLength(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try { return Integer.parseInt(line.substring(line.indexOf(':') + 1).trim()); }
                catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    // ── Dispatching ────────────────────────────────────────────────────────────

    private HttpResponse dispatch(HttpRequest req) {
        HttpServer.Handler handler = routes.get(new RouteKey(req.getMethod(), req.getPath()));
        HttpResponse res = new HttpResponse();
        if (handler == null) {
            return res.status(404, "Not Found").body("No handler for " + req.getMethod() + " " + req.getPath());
        }
        try {
            handler.handle(req, res);
        } catch (Exception e) {
            return error500(e);
        }
        return res;
    }

    // ── Writing ────────────────────────────────────────────────────────────────

    private void send(HttpResponse res) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(res.toBytes());
        while (buf.hasRemaining()) channel.write(buf);
    }

    private HttpResponse error500(Exception e) {
        return new HttpResponse().status(500, "Internal Server Error")
                .body("Internal Server Error: " + e.getMessage());
    }
}
