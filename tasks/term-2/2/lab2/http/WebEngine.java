package lab2.http;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebEngine {
    private final String bindAddress;
    private final int serverPort;
    private final ExecutorService threadPool;
    private boolean active = false;

    // Структура: HTTP_МЕТОД -> (URI -> Обработчик)
    private final Map<String, Map<String, HttpRouteHandler>> endpoints = new HashMap<>();

    public WebEngine(String bindAddress, int serverPort, int workersCount, boolean useVirtualThreads) {
        this.bindAddress = bindAddress;
        this.serverPort = serverPort;
        if (useVirtualThreads) {
            this.threadPool = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            this.threadPool = Executors.newFixedThreadPool(workersCount);
        }
    }

    public void registerRoute(String httpMethod, String uri, HttpRouteHandler action) {
        endpoints.computeIfAbsent(httpMethod.toUpperCase(), key -> new HashMap<>()).put(uri, action);
    }

    public void launch() {
        try (ServerSocketChannel channel = ServerSocketChannel.open()) {
            channel.bind(new InetSocketAddress(bindAddress, serverPort));
            active = true;
            System.out.println("WebEngine listening on port: " + serverPort);

            while (active) {
                SocketChannel connection = channel.accept();
                threadPool.execute(() -> processConnection(connection));
            }
        } catch (Exception ex) {
            System.err.println("Fatal server error: " + ex.getMessage());
        }
    }

    private void processConnection(SocketChannel connection) {
        try (connection) {
            ByteBuffer nioBuffer = ByteBuffer.allocate(4096);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            while (connection.read(nioBuffer) > 0) {
                nioBuffer.flip();
                byte[] dataChunk = new byte[nioBuffer.remaining()];
                nioBuffer.get(dataChunk);
                stream.write(dataChunk);
                nioBuffer.clear();

                if (stream.toString(StandardCharsets.UTF_8).contains("\r\n\r\n")) {
                    break;
                }
            }

            if (stream.size() == 0) return;

            String rawHttpData = stream.toString(StandardCharsets.UTF_8);
            IncomingRequest req = decodeClientPayload(rawHttpData);

            if (req != null) {
                ServerResponse resp = routeRequest(req);
                connection.write(ByteBuffer.wrap(resp.generateBytes()));
            }

        } catch (Exception ex) {
            // Игнорируем мелкие обрывы связи
        }
    }

    private IncomingRequest decodeClientPayload(String rawHttpData) {
        String[] blocks = rawHttpData.split("\r\n\r\n", 2);
        String headerBlock = blocks[0];
        String bodyBlock = blocks.length > 1 ? blocks[1] : "";

        String[] headerLines = headerBlock.split("\r\n");
        if (headerLines.length == 0) return null;

        String[] startLine = headerLines[0].split(" ");
        if (startLine.length < 2) return null;

        String method = startLine[0];
        String target = startLine[1];

        Map<String, String> mappedHeaders = new HashMap<>();
        for (int j = 1; j < headerLines.length; j++) {
            int colonPos = headerLines[j].indexOf(":");
            if (colonPos != -1) {
                String key = headerLines[j].substring(0, colonPos).trim().toLowerCase();
                String val = headerLines[j].substring(colonPos + 1).trim();
                mappedHeaders.put(key, val);
            }
        }

        Map<String, String> formFields = new HashMap<>();
        String cType = mappedHeaders.get("content-type");

        if (cType != null && cType.contains("multipart/form-data")) {
            extractMultipart(bodyBlock, cType, formFields);
        }

        return new IncomingRequest(method, target, mappedHeaders, bodyBlock, formFields);
    }

    private void extractMultipart(String bodyText, String contentType, Map<String, String> formFields) {
        try {
            String boundMarker = "--" + contentType.split("boundary=")[1];
            String[] segments = bodyText.split(boundMarker);

            for (String segment : segments) {
                if (segment.length() < 5 || segment.equals("--\r\n")) continue;

                int divider = segment.indexOf("\r\n\r\n");
                if (divider != -1) {
                    String meta = segment.substring(0, divider);
                    String content = segment.substring(divider + 4).trim();

                    int nameTag = meta.indexOf("name=\"");
                    if (nameTag != -1) {
                        int endQuote = meta.indexOf("\"", nameTag + 6);
                        String fieldName = meta.substring(nameTag + 6, endQuote);
                        formFields.put(fieldName, content);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private ServerResponse routeRequest(IncomingRequest req) {
        Map<String, HttpRouteHandler> methodsMap = endpoints.get(req.getReqMethod());
        if (methodsMap != null) {
            HttpRouteHandler handler = methodsMap.get(req.getUriTarget());
            if (handler != null) {
                try {
                    return handler.execute(req);
                } catch (Exception e) {
                    return new ServerResponse(500, "Server Crash", "{\"error\": \"Exception thrown\"}");
                }
            }
        }
        return new ServerResponse(404, "Not Found", "{\"error\": \"Endpoint not registered\"}");
    }
}