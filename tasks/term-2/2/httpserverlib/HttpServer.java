package httpserverlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private final String serverHost;
    private final int serverPort;
    private final int poolSize;
    private final boolean enableVirtualThreads;

    private final Map<HttpMethod, Map<String, HttpHandler>> routeRegistry = new ConcurrentHashMap<>();

    private ServerSocketChannel serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isRunning;

    public HttpServer(String host, int port, int threads, boolean useVirtual) {
        this.serverHost = host;
        this.serverPort = port;
        this.poolSize = threads;
        this.enableVirtualThreads = useVirtual;
    }

    public HttpServer addListener(String path, HttpMethod method, HttpHandler handler) {
        routeRegistry
                .computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .put(path, handler);
        return this;
    }

    public void start() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(serverHost, serverPort));
        serverSocket.configureBlocking(true);
        isRunning = true;

        threadPool = enableVirtualThreads
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(poolSize);

        System.out.println("✓ Сервер запущен: " + serverHost + ":" + serverPort);

        while (isRunning) {
            try {
                SocketChannel clientConnection = serverSocket.accept();
                threadPool.submit(() -> processClient(clientConnection));
            } catch (IOException e) {
                if (isRunning) e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;

        if (serverSocket != null && serverSocket.isOpen()) {
            serverSocket.close();
        }

        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processClient(SocketChannel client) {
        try {
            Request incomingRequest = parseHttpRequest(client);
            Response outgoingResponse = new Response();

            HttpHandler handler = locateHandler(incomingRequest.method, incomingRequest.path);
            if (handler != null) {
                handler.handle(incomingRequest, outgoingResponse);
            } else {
                outgoingResponse.setStatus(404);
                outgoingResponse.setBody("404 Not Found");
            }

            sendHttpResponse(client, outgoingResponse);
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private Request parseHttpRequest(SocketChannel channel) throws IOException {
        InputStream input = channel.socket().getInputStream();

        // 1. Читаем статус-линию (первая строка)
        String statusLine = readLine(input);
        if (statusLine == null || statusLine.isEmpty()) {
            throw new IOException("Пустая строка запроса");
        }

        String[] tokens = statusLine.split(" ");
        if (tokens.length < 3) {
            throw new IOException("Некорректная строка запроса: " + statusLine);
        }

        HttpMethod httpMethod = HttpMethod.valueOf(tokens[0].toUpperCase());
        String fullUri = tokens[1];

        int querySeparator = fullUri.indexOf('?');
        String resourcePath = querySeparator >= 0 ? fullUri.substring(0, querySeparator) : fullUri;
        String queryString = querySeparator >= 0 ? fullUri.substring(querySeparator + 1) : null;

        Map<String, String> headerMap = new HashMap<>();
        int contentLength = -1;
        String headerLine;

        while (!(headerLine = readLine(input)).isEmpty()) {
            int colonPos = headerLine.indexOf(':');
            if (colonPos > 0) {
                String key = headerLine.substring(0, colonPos).trim().toLowerCase();
                String value = headerLine.substring(colonPos + 1).trim();
                headerMap.put(key, value);
                if ("content-length".equals(key)) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }

        byte[] requestBody = new byte[0];
        if (contentLength > 0) {
            requestBody = input.readNBytes(contentLength);
        }

        Map<String, String> queryParams = extractQueryParameters(queryString);
        Map<String, String> multipartData = parseMultipartIfPresent(headerMap, requestBody);

        return new Request(httpMethod, resourcePath, headerMap, requestBody, queryParams, multipartData);
    }

    private String readLine(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;

        while ((b = input.read()) != -1) {
            if (b == '\r') {
                int next = input.read();
                if (next == '\n') {
                    break;
                } else {
                    sb.append((char) b);
                    if (next != -1) sb.append((char) next);
                }
            } else if (b == '\n') {
                break;
            } else {
                sb.append((char) b);
            }
        }

        return sb.toString();
    }

    private Map<String, String> extractQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
        return params;
    }

    private Map<String, String> parseMultipartIfPresent(Map<String, String> headers, byte[] body) {
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/form-data")) {
            String boundary = extractBoundaryValue(contentType);
            if (boundary != null) {
                return parseMultipartBody(body, boundary);
            }
        }
        return new HashMap<>();
    }

    private String extractBoundaryValue(String contentType) {
        for (String part : contentType.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "boundary=", 0, 9)) {
                return trimmed.substring(9).replace("\"", "");
            }
        }
        return null;
    }

    private Map<String, String> parseMultipartBody(byte[] body, String boundary) {
        Map<String, String> fields = new HashMap<>();
        String bodyText = new String(body, StandardCharsets.UTF_8);
        String delimiter = "--" + boundary;

        String[] parts = bodyText.split(delimiter);
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty() || trimmed.equals("--")) continue;

            int headerEnd = part.indexOf("\r\n\r\n");
            if (headerEnd < 0) continue;

            String headersSection = part.substring(0, headerEnd);
            String content = part.substring(headerEnd + 4);
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            }

            String fieldName = null;
            for (String header : headersSection.split("\r\n")) {
                if (header.toLowerCase().startsWith("content-disposition:")) {
                    int nameStart = header.indexOf("name=\"");
                    if (nameStart >= 0) {
                        int nameEnd = header.indexOf("\"", nameStart + 6);
                        if (nameEnd > nameStart) {
                            fieldName = header.substring(nameStart + 6, nameEnd);
                        }
                    }
                    break;
                }
            }

            if (fieldName != null) {
                fields.put(fieldName, content);
            }
        }
        return fields;
    }

    private HttpHandler locateHandler(HttpMethod method, String path) {
        Map<String, HttpHandler> methodHandlers = routeRegistry.get(method);
        return methodHandlers != null ? methodHandlers.get(path) : null;
    }

    private void sendHttpResponse(SocketChannel channel, Response response) throws IOException {

        if (response.getBody().length > 0) {
            response.setHeader("Content-Length", String.valueOf(response.getBody().length));
        }

        String reason = resolveStatusPhrase(response.getStatus());
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("HTTP/1.1 ")
                .append(response.getStatus())
                .append(" ")
                .append(reason)
                .append("\r\n");

        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            headerBuilder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        headerBuilder.append("\r\n");

        byte[] headerBytes = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);
        writeCompletely(channel, ByteBuffer.wrap(headerBytes));

        if (response.getBody().length > 0) {
            writeCompletely(channel, ByteBuffer.wrap(response.getBody()));
        }
    }

    private void writeCompletely(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private String resolveStatusPhrase(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }
}