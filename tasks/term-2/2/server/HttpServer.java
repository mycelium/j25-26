package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    public enum HttpMethod {
        GET, POST, PUT, PATCH, DELETE
    }

    private final String host;
    private final int port;
    private final ExecutorService executor;
    private final List<Route> routes = new ArrayList<>();

    private ServerSocketChannel serverChannel;
    private volatile boolean running;
    private Thread acceptThread;

    HttpServer(String host, int port, ExecutorService executor) {
        this.host = host;
        this.port = port;
        this.executor = executor;
    }

    public void addListener(String path, HttpMethod method, HttpHandler handler) {
        routes.add(new Route(path, method, handler));
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().setReuseAddress(true);
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);
        running = true;

        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    SocketChannel client = serverChannel.accept();
                    executor.submit(() -> handleConnection(client));
                } catch (ClosedChannelException e) {
                    // normal shutdown
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();

        System.out.println("HTTP Server started on " + host + ":" + port);
    }

    public void stop() throws IOException {
        running = false;
        if (serverChannel != null) serverChannel.close();
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("HTTP Server stopped");
    }

    private void handleConnection(SocketChannel channel) {
        try (channel) {
            byte[] data = readRequest(channel);
            if (data.length == 0) return;

            HttpRequest request = HttpRequest.parse(data);
            HttpResponse response = new HttpResponse();

            HttpHandler handler = findHandler(request, request.getPath(),
                    HttpMethod.valueOf(request.getMethod()));

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    response = new HttpResponse();
                    response.setStatus(500, "Internal Server Error").setBody("Internal Server Error");
                }
            } else {
                response.setStatus(404, "Not Found").setBody("404 Not Found");
            }

            channel.write(ByteBuffer.wrap(response.toBytes()));

        } catch (IOException e) {
            // connection closed or reset, ignore
        }
    }

    private byte[] readRequest(SocketChannel channel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        while (true) {
            buffer.clear();
            int read = channel.read(buffer);
            if (read <= 0) break;

            buffer.flip();
            baos.write(buffer.array(), 0, buffer.limit());

            byte[] data = baos.toByteArray();
            int headerEnd = findHeaderEnd(data);

            if (headerEnd >= 0) {
                int contentLength = parseContentLength(new String(data, 0, headerEnd));
                int bodyReceived = data.length - headerEnd - 4;

                while (bodyReceived < contentLength) {
                    buffer.clear();
                    read = channel.read(buffer);
                    if (read <= 0) break;
                    buffer.flip();
                    baos.write(buffer.array(), 0, buffer.limit());
                    bodyReceived += read;
                }
                break;
            }
        }

        return baos.toByteArray();
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n')
                return i;
        }
        return -1;
    }

    private int parseContentLength(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    return Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private HttpHandler findHandler(HttpRequest request, String path, HttpMethod method) {
        for (Route route : routes) {
            if (route.method != method) continue;
            Map<String, String> params = matchPath(route.path, path);
            if (params != null) {
                request.setPathParams(params);
                return route.handler;
            }
        }
        return null;
    }

    private Map<String, String> matchPath(String pattern, String path) {
        String[] pp = pattern.split("/");
        String[] rp = path.split("/");
        if (pp.length != rp.length) return null;

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pp.length; i++) {
            if (pp[i].startsWith("{") && pp[i].endsWith("}")) {
                params.put(pp[i].substring(1, pp[i].length() - 1), rp[i]);
            } else if (!pp[i].equals(rp[i])) {
                return null;
            }
        }
        return params;
    }

    private static class Route {
        final String path;
        final HttpMethod method;
        final HttpHandler handler;

        Route(String path, HttpMethod method, HttpHandler handler) {
            this.path = path;
            this.method = method;
            this.handler = handler;
        }
    }
}
