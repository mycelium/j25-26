package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Server {
    private final String host;
    private final int port;
    private final int threads;
    private final boolean isVirtual;
    private ExecutorService executor;
    private final Map<String, Map<Method, Handler>> routes = new HashMap<>();


    public enum Method {
        GET, POST, PUT, PATCH, DELETE
    }

    @FunctionalInterface
    public interface Handler {
        Response handle(Request request) throws Exception;
    }

    public static class Request {
        private final Method method;
        private final String path;
        private final String query;
        private final String version;
        private final Map<String, String> headers;
        private final byte[] body;

        public Request(
                Method method,
                String path,
                String query,
                String version,
                Map<String, String> headers,
                byte[] body
        ) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.version = version;
            this.headers = headers;
            this.body = body;
        }

        public String version() {
            return version;
        }

        public String bodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        public String header(String name) {
            return headers.get(name.toLowerCase());
        }

        public Method method() {
            return method;
        }

        public String path() {
            return path;
        }

        public String query() {
            return query;
        }

        public Map<String, String> headers() {
            return headers;
        }

        public byte[] body() {
            return body;
        }
    }

    public static class Response {
        private final int statusCode;
        private final String statusText;
        private final Map<String, String> headers;
        private final byte[] body;

        public Response(int statusCode, String statusText, byte[] body) {
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.body = body == null ? new byte[0] : body;
            this.headers = new HashMap<>();

            headers.put("Content-Length", String.valueOf(this.body.length));
            headers.put("Connection", "close");
        }

        public Response header(String name, String value) {
            headers.put(name, value);
            return this;
        }


        public static Response ok(String text) {
            return new Response(200, "OK", text.getBytes()).header("Content-Type", "text/plain; charset=utf-8");
        }

        public static Response methodNotAllowed() {
            return new Response(
                    405, "Method Not Allowed", "405 Method Not Allowed".getBytes()
            );
        }

        public static Response notFound() {
            return new Response(404, "Not Found", "404 Not Found".getBytes());
        }

        public static Response internalServerError() {
            return new Response(
                    500, "Internal Server Error", "500 Internal Server Error".getBytes()
            );
        }


        public byte[] toBytes() {
            StringBuilder builder = new StringBuilder();

            builder.append("HTTP/1.1 ")
                    .append(statusCode)
                    .append(" ")
                    .append(statusText)
                    .append("\r\n");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.append(header.getKey())
                        .append(": ")
                        .append(header.getValue())
                        .append("\r\n");
            }

            builder.append("\r\n");

            byte[] head = builder.toString().getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[head.length + body.length];

            System.arraycopy(head, 0, result, 0, head.length);
            System.arraycopy(body, 0, result, head.length, body.length);

            return result;
        }
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 8080;
        private int threads = 4;
        private boolean isVirtual = false;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder virtualThreads(boolean isVirtual) {
            this.isVirtual = isVirtual;
            return this;
        }

        public Server build() {
            return new Server(host, port, threads, isVirtual);
        }

    }


    private Server(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threads = threads;
        this.isVirtual = isVirtual;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void get(String path, Handler handler) {
        addRoute(path, Method.GET, handler);
    }

    public void post(String path, Handler handler) {
        addRoute(path, Method.POST, handler);
    }

    public void put(String path, Handler handler) {
        addRoute(path, Method.PUT, handler);
    }

    public void patch(String path, Handler handler) {
        addRoute(path, Method.PATCH, handler);
    }

    public void delete(String path, Handler handler) {
        addRoute(path, Method.DELETE, handler);
    }

    private void addRoute(String path, Method method, Handler handler) {
        routes.computeIfAbsent(path, p -> new HashMap<>()).put(method, handler);
    }

    private Response process(Request request) throws Exception {
        Map<Method, Handler> methods = routes.get(request.path());

        if (methods == null) {
            return Response.notFound();
        }

        Handler handler = methods.get(request.method());

        if (handler == null) {
            return Response.methodNotAllowed();
        }

        return handler.handle(request);
    }

    private Request parseRequest(SocketChannel client) throws IOException {
        ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        int contentLength = 0;
        boolean headersParsed = false;
        int headerEndIndex = -1;

        while (true) {
            int read = client.read(buffer);

            if (read == -1) {
                break;
            }

            buffer.flip();

            while (buffer.hasRemaining()) {
                rawRequest.write(buffer.get());
            }

            buffer.clear();

            byte[] data = rawRequest.toByteArray();

            if (!headersParsed) {
                headerEndIndex = findHeaderEnd(data);

                if (headerEndIndex != -1) {
                    String headersPart = new String(data, 0, headerEndIndex, StandardCharsets.UTF_8);
                    contentLength = getContentLength(headersPart);
                    headersParsed = true;
                }
            }

            if (headersParsed) {
                int bodyStart = headerEndIndex + 4;
                int currentBodyLength = data.length - bodyStart;

                if (currentBodyLength >= contentLength) {
                    break;
                }
            }
        }

        byte[] data = rawRequest.toByteArray();
        headerEndIndex = findHeaderEnd(data);

        if (headerEndIndex == -1) {
            throw new IOException("Invalid HTTP request");
        }

        String headersPart = new String(data, 0, headerEndIndex, StandardCharsets.UTF_8);
        String[] lines = headersPart.split("\r\n");

        String[] requestLine = lines[0].split(" ");

        Method method = Method.valueOf(requestLine[0]);
        String fullPath = requestLine[1];
        String version = requestLine[2];

        String path = fullPath;
        String query = "";

        int queryIndex = fullPath.indexOf("?");

        if (queryIndex != -1) {
            path = fullPath.substring(0, queryIndex);
            query = fullPath.substring(queryIndex + 1);
        }

        Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            int separator = lines[i].indexOf(":");

            if (separator != -1) {
                String name = lines[i].substring(0, separator).trim().toLowerCase();
                String value = lines[i].substring(separator + 1).trim();
                headers.put(name, value);
            }
        }

        int bodyStart = headerEndIndex + 4;
        int bodyLength = Math.max(0, data.length - bodyStart);

        byte[] body = new byte[bodyLength];
        System.arraycopy(data, bodyStart, body, 0, bodyLength);

        return new Request(method, path, query, version, headers, body);
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r'
                    && data[i + 1] == '\n'
                    && data[i + 2] == '\r'
                    && data[i + 3] == '\n') {
                return i;
            }
        }

        return -1;
    }

    private int getContentLength(String headersPart) {
        String[] lines = headersPart.split("\r\n");

        for (String line : lines) {
            if (line.toLowerCase().startsWith("content-length:")) {
                return Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }
        return 0;
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            Request request = parseRequest(client);
            Response response = process(request);
            client.write(java.nio.ByteBuffer.wrap(response.toBytes()));
        } catch (Exception e) {
            try {
                client.write(java.nio.ByteBuffer.wrap(Response.internalServerError().toBytes()));
            } catch (IOException ignored) {
            }
        }
    }


    public void start() throws IOException {
        executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threads);

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(host, port));

        while (true) {
            SocketChannel client = server.accept();
            executor.submit(() -> handleClient(client));
        }
    }
}