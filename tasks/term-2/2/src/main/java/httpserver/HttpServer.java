package httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class HttpServer {
    private final String host;
    private final int port;
    private final int threads;
    private final boolean virtual;
    private final Map<String, HttpHandler> routes = new ConcurrentHashMap<>();
    private volatile boolean active = false;
    private ServerSocketChannel server;
    private ExecutorService pool;

    public HttpServer(String host, int port, int threads, boolean virtual) {
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.virtual = virtual;
    }

    public void addRoute(String path, String method, HttpHandler handler) {
        routes.put(method.toUpperCase() + ":" + path, handler);
    }

    public void start() throws IOException {
        if (active) return;
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(host, port));
        active = true;

        if (virtual) {
            try {
                pool = (ExecutorService) Executors.class
                        .getMethod("newVirtualThreadPerTaskExecutor")
                        .invoke(null);
            } catch (Exception e) {
                pool = Executors.newFixedThreadPool(threads);
            }
        } else {
            pool = Executors.newFixedThreadPool(threads);
        }

        Thread mainLoop = new Thread(() -> {
            while (active) {
                try {
                    SocketChannel client = server.accept();
                    if (client != null)
                        pool.execute(() -> process(client));
                } catch (IOException e) {
                    if (active) e.printStackTrace();
                }
            }
        });
        mainLoop.setDaemon(true);
        mainLoop.start();
    }

    public void stop() throws IOException {
        active = false;
        if (server != null) server.close();
        if (pool != null) pool.shutdown();
    }

    private void process(SocketChannel client) {
        try {
            client.configureBlocking(true);
            HttpRequest req = parse(client);
            HttpResponse resp = new HttpResponse();

            String key = req.getMethod().toUpperCase() + ":"
                    + req.getPath().split("\\?")[0];
            HttpHandler handler = routes.get(key);
            if (handler == null) {
                resp.setStatusCode(404);
                resp.setBody("Not Found");
            } else {
                handler.handle(req, resp);
            }

            ByteBuffer buf = ByteBuffer.wrap(resp.toBytes());
            while (buf.hasRemaining()) client.write(buf);
        } catch (Exception ignored) {
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private HttpRequest parse(SocketChannel client) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8192);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int endHeaders = -1;
        while (endHeaders == -1) {
            int r = client.read(buf);
            if (r == -1) throw new IOException("Connection closed");
            buf.flip();
            byte[] chunk = new byte[r];
            buf.get(chunk);
            raw.write(chunk);
            endHeaders = findEnd(raw.toByteArray());
            buf.clear();
        }

        byte[] headerPart = Arrays.copyOf(raw.toByteArray(), endHeaders);
        String headerStr = new String(headerPart, StandardCharsets.ISO_8859_1);
        String[] lines = headerStr.split("\r\n");
        if (lines.length == 0) throw new IOException("Empty request");

        String[] firstLine = lines[0].split(" ", 3);
        if (firstLine.length < 2) throw new IOException("Bad request line");
        String method = firstLine[0];
        String path = firstLine.length > 1 ? firstLine[1] : "/";

        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                String name = lines[i].substring(0, colon).trim().toLowerCase();
                String value = lines[i].substring(colon + 1).trim();
                headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            }
        }

        byte[] body = new byte[0];
        List<String> cl = headers.get("content-length");
        if (cl != null && !cl.isEmpty()) {
            int len = Integer.parseInt(cl.get(0));
            if (len > 0) {
                ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
                int bodyStart = endHeaders + 4;
                byte[] all = raw.toByteArray();
                if (all.length > bodyStart)
                    bodyStream.write(all, bodyStart, all.length - bodyStart);
                int rest = len - (all.length - bodyStart);
                if (rest > 0) {
                    ByteBuffer bodyBuf = ByteBuffer.allocate(rest);
                    while (bodyBuf.hasRemaining()) {
                        if (client.read(bodyBuf) == -1) break;
                    }
                    bodyBuf.flip();
                    byte[] extra = new byte[bodyBuf.remaining()];
                    bodyBuf.get(extra);
                    bodyStream.write(extra);
                }
                body = bodyStream.toByteArray();
            }
        }
        return new HttpRequest(method, path, headers, body);
    }

    private int findEnd(byte[] data) {
        byte[] marker = "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);
        outer:
        for (int i = 0; i <= data.length - marker.length; i++) {
            for (int j = 0; j < marker.length; j++) {
                if (data[i + j] != marker[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}