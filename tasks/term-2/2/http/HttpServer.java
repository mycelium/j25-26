package http;
import java.util.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class HttpServer {
    
    private final int port;
    private final String host;
    private final boolean isVirtual;
    private final int numThreads;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private final Map<String, HttpHandler> routes = new HashMap<>();

    private volatile boolean running = false;
    private Thread serverThread;

    public HttpServer(String host, int port, boolean isVirtual, int numThreads) {
        this.host = host;
        this.port = port;
        this.isVirtual = isVirtual;
        this.numThreads = numThreads;
    }

    public void addRoutes(String method, String path, HttpHandler handler){
        routes.put(method + ":" + path, handler);
    }

    public void start() {
        if (running) return;
        running = true;
        serverThread = new Thread(() -> {
            try {
                serverChannel = ServerSocketChannel.open();
                serverChannel.bind(new InetSocketAddress(host, port));
                serverChannel.configureBlocking(true);
                executor = isVirtual
                        ? Executors.newVirtualThreadPerTaskExecutor()
                        : Executors.newFixedThreadPool(numThreads);
                System.out.println("HTTP server started at " + host + ":" + port);
                while (running) {
                    SocketChannel client = serverChannel.accept();
                    if (client != null) {
                        executor.submit(() -> handleClient(client));
                    }
                }
            } catch (IOException e) {
                if (!running) {
                    System.out.println("Server stopped.");
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
        serverThread.start();
    }

    public void stop() {
        try {
            running = false;
            if (serverChannel != null) {
                serverChannel.close();
            }
            if (executor != null) {
                executor.shutdown();
            }
            if (serverThread != null) {
                serverThread.join();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(SocketChannel client){
        try (client){
            
            String rawRequest = readRequest(client);
            if (rawRequest.isEmpty()) return;
            HttpRequest request = parseRequest(rawRequest);
            HttpHandler handler = findHandler(request.getMethod(), request.getPath());
            HttpResponse response = handler.handle(request);
            writeResponse(client, response);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readRequest(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        StringBuilder request = new StringBuilder();
        while (client.read(buffer) > 0) {
            buffer.flip();
            request.append(StandardCharsets.UTF_8.decode(buffer).toString());
            buffer.clear();
            if (request.indexOf("\r\n\r\n") >= 0) {
                break;
            }
        }

        int headerEnd = request.indexOf("\r\n\r\n");
        if (headerEnd < 0) {
            return request.toString();
        }

        String headersPart = request.substring(0, headerEnd);
        int contentLength = 0;
        for (String line : headersPart.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String body = request.substring(headerEnd + 4);
        int bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
        while (bodyBytes < contentLength) {
            int read = client.read(buffer);
            if (read <= 0) break;
            buffer.flip();
            String chunk = StandardCharsets.UTF_8.decode(buffer).toString();
            body += chunk;
            bodyBytes = body.getBytes(StandardCharsets.UTF_8).length;
            buffer.clear();
        }

        return headersPart + "\r\n\r\n" + body;
    }

    private HttpRequest parseRequest(String raw) {
        String[] parts = raw.split("\r\n\r\n", 2);
        String head = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        String[] lines = head.split("\r\n");
        if (lines.length < 1) {
            return null;
        }
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 3) {
            return null;
        }
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] header = lines[i].split(": ", 2);
            headers.put(header[0].trim(), header[1].trim());
        }

        String contentType = headers.getOrDefault("Content-Type", "");
        if (contentType.toLowerCase().startsWith("multipart/form-data")) {
            MultipartData multipartData = parseMultipartFormData(contentType, body);
            return new HttpRequest(method, path, version, body, headers, multipartData.fields, multipartData.files);
        }

        return new HttpRequest(method, path, version, body, headers);
    }

    private static class MultipartData {
        public final Map<String, String> fields;
        public final Map<String, byte[]> files;

        public MultipartData(Map<String, String> fields, Map<String, byte[]> files) {
            this.fields = fields;
            this.files = files;
        }
    }

    public static MultipartData parseMultipartFormData(String contentType, String body) {
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, byte[]> files = new HashMap<>();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
            return new MultipartData(values, files);
        }

        String boundaryPrefix = "boundary=";
        int boundaryIndex = contentType.indexOf(boundaryPrefix);
        if (boundaryIndex < 0) {
            return new MultipartData(values, files);
        }

        String boundary = "--" + contentType.substring(boundaryIndex + boundaryPrefix.length()).trim();
        String[] parts = body.split(boundary);

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty() || part.equals("--")) continue;

            String[] sections = part.split("\r\n\r\n", 2);
            if (sections.length < 2) continue;

            String headerBlock = sections[0];
            String valueBlock = sections[1].replaceAll("\r\n$", "");

            String name = null;
            String filename = null;
            for (String headerLine : headerBlock.split("\r\n")) {
                String lower = headerLine.toLowerCase();
                if (lower.startsWith("content-disposition:")) {
                    String[] tokens = headerLine.split(";");
                    for (String token : tokens) {
                        token = token.trim();
                        if (token.startsWith("name=")) {
                            name = token.substring(5).trim();
                            if (name.startsWith("\"") && name.endsWith("\"")) {
                                name = name.substring(1, name.length() - 1);
                            }
                        } else if (token.startsWith("filename=")) {
                            filename = token.substring(9).trim();
                            if (filename.startsWith("\"") && filename.endsWith("\"")) {
                                filename = filename.substring(1, filename.length() - 1);
                            }
                        }
                    }
                }
            }

            if (name != null) {
                if (filename != null) {
                    files.put(name, valueBlock.getBytes(StandardCharsets.UTF_8));
                } else {
                    values.put(name, valueBlock);
                }
            }
        }

        return new MultipartData(values, files);
    }

    private HttpHandler findHandler(String method, String path){
        return routes.getOrDefault(method + ":" + path,
            req -> new HttpResponse(404, "", "Not Found"));
    }

    private void writeResponse(SocketChannel client, HttpResponse response) throws IOException {
        StringBuilder rawResponse = new StringBuilder();
        int statusCode = response.getStatusCode();
        rawResponse.append("HTTP/1.1 ").append(statusCode).append(" ")
                   .append(response.getStatusText(statusCode)).append("\r\n");

        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().getBytes().length));           

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            rawResponse.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        rawResponse.append("\r\n").append(response.getBody());
        ByteBuffer buffer = ByteBuffer.wrap(rawResponse.toString().getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            client.write(buffer);
        }
    }

}
