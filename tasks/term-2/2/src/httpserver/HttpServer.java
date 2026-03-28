package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HttpServer {
    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    private final Map<RouteKey, RouteHandler> routes = new ConcurrentHashMap<>();
    private final Map<String, Set<HttpMethod>> pathMethods = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Semaphore virtualThreadLimiter;

    private volatile ServerSocketChannel serverSocketChannel;
    private volatile ExecutorService executorService;
    private volatile Thread acceptThread;

    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive");
        }

        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;
        this.virtualThreadLimiter = isVirtual ? new Semaphore(threadCount) : null;
    }

    public void addListener(HttpMethod method, String path, RouteHandler handler) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        String normalizedPath = normalizePath(path);
        routes.put(new RouteKey(method, normalizedPath), handler);
        pathMethods.computeIfAbsent(normalizedPath, ignored -> ConcurrentHashMap.newKeySet()).add(method);
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server is already running");
        }

        executorService = isVirtual
                ? Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("httpserver-vt-", 0).factory())
                : Executors.newFixedThreadPool(threadCount);

        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
        } catch (IOException exception) {
            running.set(false);
            shutdownExecutor();
            throw exception;
        }

        acceptThread = Thread.ofPlatform().name("httpserver-accept-" + port).start(this::acceptLoop);
    }

    public void stop() throws IOException {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        IOException closeException = null;
        ServerSocketChannel currentChannel = serverSocketChannel;
        if (currentChannel != null) {
            try {
                currentChannel.close();
            } catch (IOException exception) {
                closeException = exception;
            }
        }

        Thread currentAcceptThread = acceptThread;
        if (currentAcceptThread != null) {
            try {
                currentAcceptThread.join(5000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        shutdownExecutor();

        if (closeException != null) {
            throw closeException;
        }
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                SocketChannel clientChannel = serverSocketChannel.accept();
                dispatch(clientChannel);
            } catch (AsynchronousCloseException exception) {
                return;
            } catch (IOException exception) {
                if (running.get()) {
                    exception.printStackTrace();
                }
                return;
            }
        }
    }

    private void dispatch(SocketChannel clientChannel) throws IOException {
        try {
            executorService.submit(() -> handleClient(clientChannel));
        } catch (RejectedExecutionException exception) {
            clientChannel.close();
        }
    }

    private void handleClient(SocketChannel clientChannel) {
        boolean acquired = false;
        try (SocketChannel socket = clientChannel) {
            if (isVirtual) {
                virtualThreadLimiter.acquire();
                acquired = true;
            }

            HttpRequestParser.ParsedRequest parsedRequest;
            try {
                parsedRequest = HttpRequestParser.parse(socket);
            } catch (HttpRequestParser.HttpParseException exception) {
                HttpResponseWriter.write(socket, HttpResponse.text(400, "Bad Request"));
                return;
            }

            if (parsedRequest.method() == null) {
                HttpResponseWriter.write(socket, HttpResponse.text(501, "Not Implemented"));
                return;
            }

            RouteHandler handler = routes.get(new RouteKey(parsedRequest.method(), parsedRequest.path()));
            if (handler == null) {
                HttpResponse response = pathMethods.containsKey(parsedRequest.path())
                        ? HttpResponse.text(405, "Method Not Allowed")
                        : HttpResponse.text(404, "Not Found");
                HttpResponseWriter.write(socket, response);
                return;
            }

            HttpRequest request = new HttpRequest(
                    parsedRequest.method(),
                    parsedRequest.path(),
                    parsedRequest.version(),
                    parsedRequest.headers(),
                    parsedRequest.queryParams(),
                    parsedRequest.body(),
                    parsedRequest.multipartParts()
            );

            HttpResponse response;
            try {
                response = handler.handle(request);
                if (response == null) {
                    response = HttpResponse.text(500, "Internal Server Error");
                }
            } catch (Exception exception) {
                response = HttpResponse.text(500, "Internal Server Error");
            }

            HttpResponseWriter.write(socket, response);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (IOException exception) {
            // The connection is already closed or unusable.
        } finally {
            if (acquired) {
                virtualThreadLimiter.release();
            }
        }
    }

    private void shutdownExecutor() {
        ExecutorService currentExecutor = executorService;
        if (currentExecutor == null) {
            return;
        }

        currentExecutor.shutdown();
        try {
            if (!currentExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                currentExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            currentExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String normalizePath(String path) {
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
