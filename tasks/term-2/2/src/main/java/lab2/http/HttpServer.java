package lab2.http;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public final class HttpServer {
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_CONTENT_LENGTH = 10 * 1024 * 1024;
    private static final long START_TIMEOUT_SECONDS = 5;
    private static final long STOP_TIMEOUT_SECONDS = 5;

    private final String host;
    private final int port;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<ServerSocketChannel> serverChannel = new AtomicReference<>();
    private final Map<String, Map<String, HttpHandler>> routes = new ConcurrentHashMap<>();

    private volatile Thread serverThread;

    public HttpServer(String host, int port, int threadCount, boolean isVirtual) {
        if (!isVirtual && threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive for a fixed thread pool");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535");
        }

        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threadCount);
    }

    public void registerRoute(String method, String path, HttpHandler handler) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        String normalizedMethod = method.toUpperCase(Locale.ROOT);
        routes.computeIfAbsent(normalizedMethod, key -> new ConcurrentHashMap<>())
                .put(path, handler);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        CountDownLatch startupLatch = new CountDownLatch(1);
        AtomicReference<RuntimeException> startupError = new AtomicReference<>();

        serverThread = Thread.ofPlatform()
                .name("http-server-accept")
                .start(() -> acceptLoop(startupLatch, startupError));

        awaitStartup(startupLatch);

        RuntimeException error = startupError.get();
        if (error != null) {
            stop();
            throw error;
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        closeServerChannel();
        executor.shutdown();
        waitForServerThread();
    }

    private void awaitStartup(CountDownLatch startupLatch) {
        try {
            boolean started = startupLatch.await(START_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!started) {
                running.set(false);
                closeServerChannel();
                throw new IllegalStateException("HttpServer startup timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            running.set(false);
            closeServerChannel();
            throw new IllegalStateException("HttpServer startup was interrupted", exception);
        }
    }

    private void acceptLoop(CountDownLatch startupLatch, AtomicReference<RuntimeException> startupError) {
        boolean startupCompleted = false;

        try (ServerSocketChannel channel = ServerSocketChannel.open()) {
            serverChannel.set(channel);
            channel.bind(new InetSocketAddress(host, port));

            startupCompleted = true;
            startupLatch.countDown();
            System.out.println("HttpServer listening on " + host + ":" + port);

            while (running.get()) {
                try {
                    SocketChannel connection = channel.accept();
                    executor.execute(() -> processConnection(connection));
                } catch (Exception exception) {
                    if (running.get()) {
                        System.err.println("Connection accept error: " + exception.getMessage());
                    }
                }
            }
        } catch (Exception exception) {
            if (!startupCompleted) {
                startupError.set(new IllegalStateException("Could not start HttpServer", exception));
                startupLatch.countDown();
            } else if (running.get()) {
                System.err.println("Fatal server error: " + exception.getMessage());
            }
        } finally {
            running.set(false);
            serverChannel.set(null);
            startupLatch.countDown();
        }
    }

    private void waitForServerThread() {
        Thread currentServerThread = serverThread;
        if (currentServerThread == null || currentServerThread == Thread.currentThread()) {
            return;
        }

        try {
            currentServerThread.join(STOP_TIMEOUT_SECONDS * 1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeServerChannel() {
        ServerSocketChannel channel = serverChannel.getAndSet(null);
        if (channel == null) {
            return;
        }

        try {
            channel.close();
        } catch (Exception ignored) {
            // Closing the server channel is best-effort during shutdown.
        }
    }

    private void processConnection(SocketChannel connection) {
        try (connection) {
            try {
                byte[] rawRequest = readRequest(connection);
                if (rawRequest.length == 0) {
                    return;
                }

                HttpRequest request = parseRequest(rawRequest);
                HttpResponse response = routeRequest(request);
                writeFully(connection, response.toBytes());
            } catch (HttpRequestException exception) {
                writeFully(connection, exception.response().toBytes());
            }
        } catch (Exception ignored) {
            // Client disconnects and malformed network packets are ignored by design.
        }
    }

    private byte[] readRequest(SocketChannel connection) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        int headerEndIndex = -1;
        int contentLength = 0;

        while (true) {
            int bytesRead = connection.read(buffer);
            if (bytesRead == -1) {
                break;
            }

            buffer.flip();
            byte[] chunk = new byte[buffer.remaining()];
            buffer.get(chunk);
            requestBytes.write(chunk);
            buffer.clear();

            byte[] current = requestBytes.toByteArray();
            if (headerEndIndex == -1) {
                headerEndIndex = findHeaderEnd(current);
                if (headerEndIndex != -1) {
                    String headerBlock = new String(current, 0, headerEndIndex, StandardCharsets.ISO_8859_1);
                    contentLength = parseContentLength(headerBlock);
                }
            }

            if (headerEndIndex != -1) {
                int fullRequestLength = headerEndIndex + 4 + contentLength;
                if (current.length >= fullRequestLength) {
                    return copyExactLength(current, fullRequestLength);
                }
            }
        }

        return requestBytes.toByteArray();
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i <= data.length - 4; i++) {
            if (data[i] == '\r'
                    && data[i + 1] == '\n'
                    && data[i + 2] == '\r'
                    && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private int parseContentLength(String headerBlock) throws HttpRequestException {
        for (String line : headerBlock.split("\r\n")) {
            int separator = line.indexOf(':');
            if (separator == -1) {
                continue;
            }

            String headerName = line.substring(0, separator).trim();
            String headerValue = line.substring(separator + 1).trim();
            if (headerName.equalsIgnoreCase("Content-Length")) {
                try {
                    int contentLength = Integer.parseInt(headerValue);
                    if (contentLength < 0) {
                        throw new HttpRequestException(new HttpResponse(400, "{\"error\": \"Invalid Content-Length\"}"));
                    }
                    if (contentLength > MAX_CONTENT_LENGTH) {
                        throw new HttpRequestException(new HttpResponse(413, "{\"error\": \"Request body too large\"}"));
                    }
                    return contentLength;
                } catch (NumberFormatException exception) {
                    throw new HttpRequestException(new HttpResponse(400, "{\"error\": \"Invalid Content-Length\"}"));
                }
            }
        }
        return 0;
    }

    private byte[] copyExactLength(byte[] source, int length) {
        byte[] result = new byte[length];
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    private HttpRequest parseRequest(byte[] rawRequest) throws HttpRequestException {
        int headerEndIndex = findHeaderEnd(rawRequest);
        if (headerEndIndex == -1) {
            throw new HttpRequestException(new HttpResponse(400, "{\"error\": \"Malformed request\"}"));
        }

        String headerBlock = new String(rawRequest, 0, headerEndIndex, StandardCharsets.ISO_8859_1);
        int contentLength = parseContentLength(headerBlock);
        int bodyStartIndex = headerEndIndex + 4;
        int availableBodyLength = rawRequest.length - bodyStartIndex;

        if (availableBodyLength < contentLength) {
            throw new HttpRequestException(new HttpResponse(400, "{\"error\": \"Incomplete request body\"}"));
        }

        String body = new String(rawRequest, bodyStartIndex, contentLength, StandardCharsets.UTF_8);

        String[] headerLines = headerBlock.split("\r\n");
        String[] requestLine = headerLines[0].split(" ", 3);
        if (requestLine.length < 3) {
            throw new HttpRequestException(new HttpResponse(400, "{\"error\": \"Malformed request line\"}"));
        }

        String method = requestLine[0];
        String path = requestLine[1];
        String protocol = requestLine[2];

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            int separator = headerLines[i].indexOf(':');
            if (separator == -1) {
                continue;
            }

            String name = headerLines[i].substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = headerLines[i].substring(separator + 1).trim();
            headers.put(name, value);
        }

        Map<String, String> multipartFields = parseMultipartFields(body, headers.get("content-type"));
        return new HttpRequest(method, path, protocol, headers, body, multipartFields);
    }

    private Map<String, String> parseMultipartFields(String body, String contentType) {
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            return Map.of();
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null || boundary.isBlank()) {
            return Map.of();
        }

        Map<String, String> fields = new HashMap<>();
        String boundaryMarker = "--" + boundary;
        String[] parts = body.split(Pattern.quote(boundaryMarker));

        for (String part : parts) {
            if (part.isBlank() || part.equals("--\r\n") || part.equals("--")) {
                continue;
            }

            int divider = part.indexOf("\r\n\r\n");
            if (divider == -1) {
                continue;
            }

            String meta = part.substring(0, divider);
            String content = stripTrailingCrlf(part.substring(divider + 4));
            String fieldName = extractMultipartFieldName(meta);
            if (fieldName != null) {
                fields.put(fieldName, content);
            }
        }

        return fields;
    }

    private String extractBoundary(String contentType) {
        for (String parameter : contentType.split(";")) {
            String trimmed = parameter.trim();
            if (trimmed.startsWith("boundary=")) {
                String value = trimmed.substring("boundary=".length());
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    return value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }

    private String stripTrailingCrlf(String value) {
        String result = value;
        if (result.endsWith("--")) {
            result = result.substring(0, result.length() - 2);
        }
        if (result.endsWith("\r\n")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    private String extractMultipartFieldName(String meta) {
        String marker = "name=\"";
        int start = meta.indexOf(marker);
        if (start == -1) {
            return null;
        }

        int valueStart = start + marker.length();
        int end = meta.indexOf('"', valueStart);
        if (end == -1) {
            return null;
        }

        return meta.substring(valueStart, end);
    }

    private HttpResponse routeRequest(HttpRequest request) {
        boolean pathExists = routes.values().stream()
                .anyMatch(methodRoutes -> methodRoutes.containsKey(request.getPath()));

        Map<String, HttpHandler> methodRoutes = routes.get(request.getMethod());
        if (methodRoutes == null) {
            return pathExists
                    ? new HttpResponse(405, "{\"error\": \"Method not allowed\"}")
                    : new HttpResponse(404, "{\"error\": \"Endpoint not registered\"}");
        }

        HttpHandler handler = methodRoutes.get(request.getPath());
        if (handler == null) {
            return pathExists
                    ? new HttpResponse(405, "{\"error\": \"Method not allowed\"}")
                    : new HttpResponse(404, "{\"error\": \"Endpoint not registered\"}");
        }

        try {
            return handler.handle(request);
        } catch (Exception exception) {
            return new HttpResponse(500, "{\"error\": \"Exception thrown\"}");
        }
    }

    private void writeFully(SocketChannel connection, byte[] data) throws Exception {
        ByteBuffer responseBuffer = ByteBuffer.wrap(data);
        while (responseBuffer.hasRemaining()) {
            connection.write(responseBuffer);
        }
    }

    private static final class HttpRequestException extends Exception {
        private final HttpResponse response;

        private HttpRequestException(HttpResponse response) {
            this.response = response;
        }

        private HttpResponse response() {
            return response;
        }
    }
}
