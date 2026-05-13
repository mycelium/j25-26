package ru.task2.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final String host;
    private final int port;

    private final ExecutorService executor;

    private final Map<RouteKey, HttpHandler> routes =
            new HashMap<>();

    private volatile boolean running = false;

    private ServerSocketChannel server;

    public HttpServer(
            String host,
            int port,
            int threads,
            boolean isVirtual
    ) {

        this.host = host;
        this.port = port;

        if (isVirtual) {
            executor =
                    Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executor =
                    Executors.newFixedThreadPool(threads);
        }
    }

    public void addHandler(
            HttpMethod method,
            String path,
            HttpHandler handler
    ) {

        routes.put(
                new RouteKey(method, path),
                handler
        );
    }

    public void start() throws IOException {

        running = true;

        server = ServerSocketChannel.open();

        server.bind(
                new InetSocketAddress(host, port)
        );

        while (running) {

            SocketChannel client =
                    server.accept();

            executor.submit(
                    () -> handleClient(client)
            );
        }
    }

    public void stop() throws IOException {

        running = false;

        if (server != null) {
            server.close();
        }

        executor.shutdown();
    }

    private void handleClient(SocketChannel client) {

        try (client) {

            ByteBuffer buffer =
                    ByteBuffer.allocate(8192);

            client.read(buffer);

            buffer.flip();

            String raw =
                    new String(
                            buffer.array(),
                            0,
                            buffer.limit()
                    );

            HttpRequest request =
                    parseRequest(raw);

            HttpHandler handler =
                    routes.get(
                            new RouteKey(
                                    request.getMethod(),
                                    request.getPath()
                            )
                    );

            HttpResponse response;

            if (handler == null) {

                response =
                        new HttpResponse()
                                .status(404)
                                .body("Not Found");

            } else {

                response =
                        handler.handle(request);
            }

            client.write(
                    ByteBuffer.wrap(
                            response.toBytes()
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpRequest parseRequest(String raw) {

        String[] parts =
                raw.split("\r\n\r\n", 2);

        String head = parts[0];

        String body =
                parts.length > 1
                        ? parts[1]
                        : "";

        String[] lines =
                head.split("\r\n");

        if (lines.length == 0) {
            throw new RuntimeException(
                    "Invalid HTTP request"
            );
        }

        String[] requestLine =
                lines[0].split(" ");

        HttpMethod method =
                HttpMethod.valueOf(requestLine[0]);

        String path =
                requestLine[1];

        Map<String, String> headers =
                new HashMap<>();

        for (int i = 1; i < lines.length; i++) {

            String[] header =
                    lines[i].split(": ", 2);

            if (header.length == 2) {

                headers.put(
                        header[0],
                        header[1]
                );
            }
        }

        return new HttpRequest(
                method,
                path,
                headers,
                body
        );
    }
}