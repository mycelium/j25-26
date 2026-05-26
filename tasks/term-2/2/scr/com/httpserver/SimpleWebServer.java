package com.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class SimpleWebServer {
    private final String hostAddress;
    private final int listenPort;
    private final int threadPoolSize;
    private final boolean useVirtual;
    private final Map<String, Map<String, BiFunction<RequestPacket, ResponsePacket, ResponsePacket>>> handlers;

    private ServerSocketChannel socketChannel;
    private ExecutorService threadPool;
    private volatile boolean active;

    public SimpleWebServer(String hostAddress, int listenPort, int threadPoolSize, boolean useVirtual) {
        this.hostAddress = hostAddress;
        this.listenPort = listenPort;
        this.threadPoolSize = threadPoolSize;
        this.useVirtual = useVirtual;
        this.handlers = new ConcurrentHashMap<>();
        this.active = false;
    }

    public SimpleWebServer(WebServerSettings settings) {
        this(settings.getAddress(), settings.getPortNumber(),
                settings.getPoolSize(), settings.isVirtualMode());
    }

    public SimpleWebServer onGet(String path, BiFunction<RequestPacket, ResponsePacket, ResponsePacket> handler) {
        handlers.computeIfAbsent("GET", k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public SimpleWebServer onPost(String path, BiFunction<RequestPacket, ResponsePacket, ResponsePacket> handler) {
        handlers.computeIfAbsent("POST", k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public SimpleWebServer onPut(String path, BiFunction<RequestPacket, ResponsePacket, ResponsePacket> handler) {
        handlers.computeIfAbsent("PUT", k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public SimpleWebServer onPatch(String path, BiFunction<RequestPacket, ResponsePacket, ResponsePacket> handler) {
        handlers.computeIfAbsent("PATCH", k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public SimpleWebServer onDelete(String path, BiFunction<RequestPacket, ResponsePacket, ResponsePacket> handler) {
        handlers.computeIfAbsent("DELETE", k -> new ConcurrentHashMap<>()).put(path, handler);
        return this;
    }

    public void launch() {
        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.bind(new InetSocketAddress(hostAddress, listenPort));

            threadPool = useVirtual
                    ? Executors.newVirtualThreadPerTaskExecutor()
                    : Executors.newFixedThreadPool(threadPoolSize);

            active = true;
            System.out.println("Web server running at " + hostAddress + ":" + listenPort
                    + " (virtual threads=" + useVirtual + ", pool size=" + threadPoolSize + ")");

            while (active) {
                try {
                    SocketChannel clientConnection = socketChannel.accept();
                    threadPool.execute(() -> processConnection(clientConnection));
                } catch (IOException ex) {
                    if (active) {
                        throw new WebServerError("Connection accept failed", ex);
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            throw new WebServerError("Cannot start web server", ex);
        }
    }

    private void processConnection(SocketChannel clientConnection) {
        try (SocketChannel channel = clientConnection;
             InputStream input = Channels.newInputStream(channel);
             OutputStream output = Channels.newOutputStream(channel)) {

            String requestFirstLine = readInputLine(input);
            if (requestFirstLine == null || requestFirstLine.isEmpty()) return;

            String[] tokens = requestFirstLine.split(" ");
            if (tokens.length < 3) return;

            String method = tokens[0];
            String uri = tokens[1];

            Map<String, String> headers = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            String line;
            while ((line = readInputLine(input)) != null && !line.isEmpty()) {
                int colonPosition = line.indexOf(':');
                if (colonPosition > 0) {
                    String headerName = line.substring(0, colonPosition).trim();
                    String headerValue = line.substring(colonPosition + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }

            byte[] payload = new byte[0];
            String contentLen = headers.get("Content-Length");
            if (contentLen != null) {
                int length = Integer.parseInt(contentLen.trim());
                if (length > 0) {
                    payload = input.readNBytes(length);
                }
            }

            RequestPacket request = new RequestPacket(method, uri, headers, payload);
            ResponsePacket response = new ResponsePacket();

            var methodRoutes = handlers.get(method);
            if (methodRoutes != null && methodRoutes.containsKey(uri)) {
                response = methodRoutes.get(uri).apply(request, response);
            } else {
                response.withStatus(404, "Not Found").withBody("404 - Not Found");
            }

            output.write(response.serialize());
            output.flush();

        } catch (IOException ex) {
            // Silent catch - connection issues are expected and not logged
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String readInputLine(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = -1, current;
        while ((current = input.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                byte[] data = buffer.toByteArray();
                return new String(data, 0, data.length - 1, StandardCharsets.ISO_8859_1);
            }
            buffer.write(current);
            previous = current;
        }
        if (buffer.size() == 0) return null;
        return buffer.toString(StandardCharsets.ISO_8859_1);
    }

    public void shutdown() {
        active = false;
        try {
            if (socketChannel != null) socketChannel.close();
        } catch (IOException ignored) {
        }
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}