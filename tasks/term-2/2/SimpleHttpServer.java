import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class SimpleHttpServer {
    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running = false;

    private final Map<String, Map<String, HttpHandler>> handlers = new HashMap<>();

    public SimpleHttpServer(String host, int port) {
        this(host, port, 10, false);
    }

    public SimpleHttpServer(String host, int port, int threadCount) {
        this(host, port, threadCount, false);
    }

    public SimpleHttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;

        handlers.put("GET", new HashMap<>());
        handlers.put("POST", new HashMap<>());
        handlers.put("PUT", new HashMap<>());
        handlers.put("PATCH", new HashMap<>());
        handlers.put("DELETE", new HashMap<>());
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        if (isVirtual) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executor = Executors.newFixedThreadPool(threadCount);
        }

        running = true;
        System.out.println("Server started on " + host + ":" + port);
        System.out.println("Virtual threads: " + isVirtual + ", thread pool size: " + threadCount);

        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                executor.submit(() -> handleClient(clientChannel));
            } catch (ClosedChannelException e) {
                if (running) e.printStackTrace();
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
        System.out.println("Server stopped");
    }

    public void get(String path, HttpHandler handler) { handlers.get("GET").put(path, handler); }
    public void post(String path, HttpHandler handler) { handlers.get("POST").put(path, handler); }
    public void put(String path, HttpHandler handler) { handlers.get("PUT").put(path, handler); }
    public void patch(String path, HttpHandler handler) { handlers.get("PATCH").put(path, handler); }
    public void delete(String path, HttpHandler handler) { handlers.get("DELETE").put(path, handler); }

    private void handleClient(SocketChannel clientChannel) {
        try (clientChannel) {
            InputStream in = clientChannel.socket().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            System.out.println("Request: " + requestLine);

            HttpRequest request = parseRequestLine(requestLine);
            Map<String, String> headers = parseHeaders(reader);
            request.setHeaders(headers);
            String body = parseBody(reader, headers);
            request.setBody(body);

            HttpResponse response = new HttpResponse();
            HttpHandler handler = findHandler(request.getMethod(), request.getPath());

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.status(500);
                    response.send("Internal Server Error: " + e.getMessage());
                }
            } else {
                response.status(404);
                response.send("Not Found: " + request.getPath());
            }

            sendResponse(clientChannel, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpRequest parseRequestLine(String requestLine) {
        HttpRequest request = new HttpRequest();
        String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
            request.setMethod(parts[0]);
            String fullPath = parts[1];
            int queryIndex = fullPath.indexOf('?');
            if (queryIndex != -1) {
                request.setPath(fullPath.substring(0, queryIndex));
                parseQueryString(request, fullPath.substring(queryIndex + 1));
            } else {
                request.setPath(fullPath);
            }
        }
        return request;
    }

    private void parseQueryString(HttpRequest request, String queryString) {
        for (String pair : queryString.split("&")) {
            String[] kv = pair.split("=", 2);
            request.getParams().put(kv[0], kv.length == 2 ? kv[1] : "");
        }
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                headers.put(parts[0].toLowerCase().trim(), parts[1].trim());
            }
        }
        return headers;
    }

    private String parseBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        String contentLengthStr = headers.get("content-length");
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int read = 0;
                while (read < contentLength) {
                    int result = reader.read(bodyChars, read, contentLength - read);
                    if (result == -1) break;
                    read += result;
                }
                return new String(bodyChars, 0, read);
            }
        }
        return "";
    }

    private HttpHandler findHandler(String method, String path) {
        Map<String, HttpHandler> methodHandlers = handlers.get(method);
        if (methodHandlers != null && methodHandlers.containsKey(path)) {
            return methodHandlers.get(path);
        }
        return null;
    }

    private void sendResponse(SocketChannel clientChannel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append("\r\n");
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        sb.append(response.getBody());

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
    }
}
