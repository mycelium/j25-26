package com.httpserver;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class HttpServer {

    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    private final String host;
    private final int port;

    private final Map<String, Map<String, RequestHandler>> routes = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> pathMethods = new ConcurrentHashMap<>();

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private ExecutorService executor;
    private Semaphore semaphore;

    private Thread eventLoopThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public HttpServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static HttpServer create(String host, int port) {
        return new HttpServer(host, port);
    }

    // ---------------------------------------------------------------------
    // ROUTES
    // ---------------------------------------------------------------------

    public HttpServer get(String path, RequestHandler handler) {
        return addRoute("GET", path, handler);
    }

    public HttpServer post(String path, RequestHandler handler) {
        return addRoute("POST", path, handler);
    }

    public HttpServer put(String path, RequestHandler handler) {
        return addRoute("PUT", path, handler);
    }

    public HttpServer patch(String path, RequestHandler handler) {
        return addRoute("PATCH", path, handler);
    }

    public HttpServer delete(String path, RequestHandler handler) {
        return addRoute("DELETE", path, handler);
    }

    private HttpServer addRoute(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(path, p -> new ConcurrentHashMap<>()).put(method, handler);
        pathMethods.computeIfAbsent(path, p -> ConcurrentHashMap.newKeySet()).add(method);
        return this;
    }

    // ---------------------------------------------------------------------
    // START
    // ---------------------------------------------------------------------

    public void start(int threadCount, boolean useVirtualThreads) throws IOException {

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already running");
        }

        // Workers
        if (useVirtualThreads) {
            semaphore = new Semaphore(threadCount);
            executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        } else {
            semaphore = null;
            executor = Executors.newFixedThreadPool(threadCount);
        }

        // Server socket (NON-BLOCKING)
        serverChannel = ServerSocketChannel.open();
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(false);

        // Selector
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Event loop
        eventLoopThread = Thread.ofPlatform().start(this::eventLoop);

        System.out.println("Server started on http://" + host + ":" + port);
    }

    // ---------------------------------------------------------------------
    // EVENT LOOP (CORRIGÉ)
    // ---------------------------------------------------------------------

    private void eventLoop() {
        try {
            while (running.get()) {

                selector.select();

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isAcceptable()) continue;

                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();

                    if (client == null) continue;

                    client.configureBlocking(true);

                    executor.submit(() -> handleClient(client));
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                logger.severe("Event loop error: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------------
    // CLIENT HANDLING
    // ---------------------------------------------------------------------

    private void handleClient(SocketChannel clientChannel) {

        if (semaphore != null) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        try {
            byte[] raw = readRequest(clientChannel);

            HttpRequest req = HttpRequestParser.parse(raw);
            HttpResponse res = routeRequest(req);

            writeResponse(clientChannel, res);

        } catch (Exception e) {
            sendError(clientChannel, HttpResponse.internalServerError("Internal Error"));
        } finally {
            if (semaphore != null) semaphore.release();
            try { clientChannel.close(); } catch (IOException ignored) {}
        }
    }

    // ---------------------------------------------------------------------
    // ROUTING
    // ---------------------------------------------------------------------

    private HttpResponse routeRequest(HttpRequest request) {

        String method = request.getMethod().name();
        String path = request.getPath();

        Map<String, RequestHandler> methods = routes.get(path);

        if (methods == null) {
            return HttpResponse.notFound("Not Found: " + path);
        }

        RequestHandler handler = methods.get(method);

        if (handler == null) {
            Set<String> allowed = pathMethods.get(path);
            return HttpResponse.methodNotAllowed(String.join(", ", allowed));
        }

        try {
            return handler.handle(request);
        } catch (Exception e) {
            return HttpResponse.internalServerError("Handler error");
        }
    }

    // ---------------------------------------------------------------------
    // IO
    // ---------------------------------------------------------------------

    private byte[] readRequest(SocketChannel channel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int headerEnd = -1;
        int contentLength = 0;
        
        while (headerEnd < 0 || baos.size() - headerEnd < contentLength) {
            int n = channel.read(ByteBuffer.wrap(buffer));
            if (n < 0) break;
            baos.write(buffer, 0, n);
            
            if (headerEnd < 0) {
                headerEnd = findHeaderEnd(baos.toByteArray());
                if (headerEnd >= 0) {
                    contentLength = extractContentLength(baos.toByteArray(), headerEnd);
                }
            }
        }
        return baos.toByteArray();
    }

    private void writeResponse(SocketChannel channel, HttpResponse response) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(response.toBytes());
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    private void sendError(SocketChannel channel, HttpResponse res) {
        try {
            writeResponse(channel, res);
        } catch (IOException ignored) {}
    }

    // ---------------------------------------------------------------------
    // STOP
    // ---------------------------------------------------------------------

    public void stop() throws IOException, InterruptedException {

        if (!running.compareAndSet(true, false)) return;

        selector.wakeup();
        serverChannel.close();

        eventLoopThread.join(3000);

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i <= data.length - 4; i++) {
            if (data[i] == '\r' &&
                data[i + 1] == '\n' &&
                data[i + 2] == '\r' &&
                data[i + 3] == '\n') {
                return i + 4;
            }
        }
        return -1;
    }
    private int extractContentLength(byte[] data, int headerEnd) {

        String headers = new String(
                data,
                0,
                headerEnd,
                java.nio.charset.StandardCharsets.US_ASCII
        );

        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    return Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }

        return 0;
    }
}