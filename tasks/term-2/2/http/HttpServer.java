package http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final String host;
    private final int port;
    private final ExecutorService executor;

  
    private final Map<String, Map<String, RequestHandler>> routes = new HashMap<>();


    public HttpServer(String host, int port, int threads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        if (isVirtual) {
      
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            this.executor = Executors.newFixedThreadPool(threads);
        }
    }


    public void addHandler(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method.toUpperCase(), handler);
    }

    
    public void start() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(host, port));
            System.out.println("Server started on " + host + ":" + port);

            while (!Thread.currentThread().isInterrupted()) {
                SocketChannel clientChannel = serverChannel.accept();
                executor.submit(() -> handleClient(clientChannel));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(SocketChannel clientChannel) {
        try (clientChannel) {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead <= 0) return;

            buffer.flip();
            byte[] requestBytes = new byte[buffer.remaining()];
            buffer.get(requestBytes);
            String rawRequest = new String(requestBytes, StandardCharsets.UTF_8);

            HttpRequest request = parseRequest(rawRequest);
            if (request == null) return;

            HttpResponse response;
            Map<String, RequestHandler> pathHandlers = routes.get(request.getPath());

            if (pathHandlers != null && pathHandlers.containsKey(request.getMethod())) {
                response = pathHandlers.get(request.getMethod()).handle(request);
            } else {
                response = new HttpResponse(404, "Not Found", "Route not found");
            }

            sendResponse(clientChannel, response);
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    private HttpRequest parseRequest(String rawRequest) {
        String[] lines = rawRequest.split("\r\n");
        if (lines.length == 0) return null;

        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 3) return null;

        String method = requestLine[0];
        String path = requestLine[1];

        Map<String, String> headers = new HashMap<>();
        int i = 1;
        while (i < lines.length && !lines[i].isEmpty()) {
            String[] headerParts = lines[i].split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].toLowerCase(), headerParts[1]);
            }
            i++;
        }

        String body = "";
        Map<String, String> formData = new HashMap<>(); 

        if (headers.containsKey("content-length")) {
            int bodyStartIndex = rawRequest.indexOf("\r\n\r\n") + 4;
            if (bodyStartIndex > 3 && bodyStartIndex < rawRequest.length()) {
                body = rawRequest.substring(bodyStartIndex);
            }

    
            String contentType = headers.get("content-type");
            if (contentType != null && contentType.contains("multipart/form-data")) {
                try {
                  
                    String boundary = "--" + contentType.split("boundary=")[1];
                    String[] parts = body.split(boundary);

                    for (String part : parts) {
                 
                        if (part.isEmpty() || part.equals("--\r\n") || part.equals("--")) continue;

                        int headerEnd = part.indexOf("\r\n\r\n");
                        if (headerEnd != -1) {
                            String partHeaders = part.substring(0, headerEnd);
                         
                            String partBody = part.substring(headerEnd + 4).trim();

                       
                            int nameIndex = partHeaders.indexOf("name=\"");
                            if (nameIndex != -1) {
                                int nameEnd = partHeaders.indexOf("\"", nameIndex + 6);
                                String name = partHeaders.substring(nameIndex + 6, nameEnd);
                                formData.put(name, partBody);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse multipart/form-data: " + e.getMessage());
                }
            }
        }

        return new HttpRequest(method, path, headers, body, formData);
    }

    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append("\r\n");

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        String body = response.getBody();
        if (body != null && !body.isEmpty()) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n\r\n");
            sb.append(body);
        } else {
            sb.append("Content-Length: 0\r\n\r\n");
        }

        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}