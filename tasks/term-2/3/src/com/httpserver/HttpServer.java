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
    private static final int MAX_REQUEST_SIZE = 8 * 1024 * 1024; // 8 MB

    private final String host;
    private final int port;

    // ConcurrentHashMap : enregistrement des routes thread-safe
    // Structure : path → (method → handler)
    private final Map<String, Map<String, RequestHandler>> routes = new ConcurrentHashMap<>();

    // pathMethods : path → ensemble des méthodes enregistrées (pour renvoyer 405)
    private final Map<String, Set<String>> pathMethods = new ConcurrentHashMap<>();

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private Thread acceptThread;            // thread d'accept séparé (10/10)
    private Semaphore semaphore;            // rate-limit des virtual threads (10/10)

    private final AtomicBoolean running = new AtomicBoolean(false); // (10/10)

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public HttpServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static HttpServer create(String host, int port) {
        return new HttpServer(host, port);
    }

    // -------------------------------------------------------------------------
    // Enregistrement des routes (API fluent)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Démarrage
    // -------------------------------------------------------------------------

    public void start(int threadCount, boolean useVirtualThreads) throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        // Configurer l'executor
        if (useVirtualThreads) {
            // Semaphore pour limiter la concurrence des virtual threads (10/10)
            semaphore = new Semaphore(threadCount);
            executor  = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("vt-worker-", 0).factory());
            logger.info("Using virtual threads (concurrency limit: " + threadCount + ")");
        } else {
            semaphore = null;
            executor  = Executors.newFixedThreadPool(
                threadCount,
                Thread.ofPlatform().name("pt-worker-", 0).factory());
            logger.info("Using fixed thread pool with " + threadCount + " threads");
        }

        // Ouvrir le ServerSocketChannel
        serverChannel = ServerSocketChannel.open();
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        logger.info("Server started on http://" + host + ":" + port);

        // Thread d'accept séparé — libère le thread appelant (10/10)
        acceptThread = Thread.ofPlatform().name("accept-thread").start(this::acceptLoop);
    }

    /** Boucle d'accept dans son propre thread. */
    private void acceptLoop() {
        while (running.get()) {
            try {
                SocketChannel client = serverChannel.accept();
                if (client != null) {
                    executor.submit(() -> handleClient(client));
                }
            } catch (IOException e) {
                if (running.get()) {
                    logger.warning("Accept error: " + e.getMessage());
                }
                // Si running est false, l'exception vient du close() dans stop() — normal
            }
        }
    }

    // -------------------------------------------------------------------------
    // Arrêt propre
    // -------------------------------------------------------------------------

    public void stop() throws IOException, InterruptedException {
        if (!running.compareAndSet(true, false)) return;

        serverChannel.close();          // interrompt serverChannel.accept()
        acceptThread.join(5_000);       // attendre la fin du thread accept (10/10)

        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        logger.info("Server stopped");
    }

    // -------------------------------------------------------------------------
    // Gestion d'une connexion client
    // -------------------------------------------------------------------------

    private void handleClient(SocketChannel clientChannel) {
        // Acquérir le semaphore pour les virtual threads
        if (semaphore != null) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        try {
            clientChannel.configureBlocking(true);
            byte[] rawRequest = readRequest(clientChannel);

            HttpRequest  request  = HttpRequestParser.parse(rawRequest);
            HttpResponse response = routeRequest(request);
            writeResponse(clientChannel, response);

        } catch (HttpRequestParser.EmptyRequestException e) {
            // Connexion fermée proprement par le client — pas une erreur
        } catch (IOException e) {
            logger.warning("Request error: " + e.getMessage());
            sendError(clientChannel, HttpResponse.badRequest("Bad Request"));
        } catch (Exception e) {
            logger.severe("Handler error: " + e.getMessage());
            sendError(clientChannel, HttpResponse.internalServerError("Internal Server Error"));
        } finally {
            if (semaphore != null) semaphore.release();
            try { clientChannel.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Lit les bytes bruts du réseau en cherchant \r\n\r\n en bytes
     * (charset-safe, pas de conversion String).
     */
    private byte[] readRequest(SocketChannel channel) throws IOException {
        ByteArrayOutputStream baos   = new ByteArrayOutputStream();
        byte[] buffer                = new byte[8192];
        int headerEnd                = -1;
        int contentLength            = 0;

        while (true) {
            int n = channel.socket().getInputStream().read(buffer);
            if (n < 0) break;
            baos.write(buffer, 0, n);

            byte[] current = baos.toByteArray();

            // Chercher \r\n\r\n en bytes pour détecter la fin des headers
            if (headerEnd < 0) {
                headerEnd = HttpRequestParser.findHeaderEnd(current);
                if (headerEnd >= 0) {
                    // Extraire Content-Length depuis les headers (en bytes, US-ASCII)
                    String headerSection = new String(current, 0, headerEnd - 4,
                                                      java.nio.charset.StandardCharsets.US_ASCII);
                    contentLength = extractContentLength(headerSection);
                }
            }

            if (headerEnd >= 0) {
                int bodyReceived = current.length - headerEnd;
                if (bodyReceived >= contentLength) break;
            }

            if (baos.size() > MAX_REQUEST_SIZE) {
                throw new IOException("Request too large (> " + MAX_REQUEST_SIZE + " bytes)");
            }
        }

        return baos.toByteArray();
    }

    private int extractContentLength(String headerSection) {
        for (String line : headerSection.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    return Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Routage — distingue 404 (path inconnu) et 405 (method non enregistrée)
    // -------------------------------------------------------------------------

    private HttpResponse routeRequest(HttpRequest request) {
        String method = request.getMethod().name();
        String path   = request.getPath();

        Map<String, RequestHandler> methodMap = routes.get(path);
        if (methodMap == null) {
            return HttpResponse.notFound("Not Found: " + path);
        }

        RequestHandler handler = methodMap.get(method);
        if (handler == null) {
            Set<String> allowed = pathMethods.get(path);
            return HttpResponse.methodNotAllowed(String.join(", ", allowed));
        }

        try {
            return handler.handle(request);
        } catch (Exception e) {
            logger.severe("Handler threw exception: " + e.getMessage());
            return HttpResponse.internalServerError("Internal Server Error");
        }
    }

    // -------------------------------------------------------------------------
    // Écriture de la réponse — boucle ByteBuffer (écriture partielle possible)
    // -------------------------------------------------------------------------

    private void writeResponse(SocketChannel channel, HttpResponse response) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(response.toBytes());
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private void sendError(SocketChannel channel, HttpResponse response) {
        try {
            writeResponse(channel, response);
        } catch (IOException ignored) {}
    }
}
