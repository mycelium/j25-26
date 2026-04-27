package com.httpserverlib;

import com.httpserverlib.config.ServerConfig;
import com.httpserverlib.core.RequestParser;
import com.httpserverlib.core.ResponseWriter;
import com.httpserverlib.core.Router;
import com.httpserverlib.handler.HttpHandler;
import com.httpserverlib.model.HttpMethod;
import com.httpserverlib.model.HttpRequest;
import com.httpserverlib.model.HttpResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
    private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());

    private final ServerConfig config;
    private final Router router;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running;

    private HttpServer(ServerConfig config, Router router) {
        this.config = config;
        this.router = router;
    }

    public static Builder create() {
        return new Builder();
    }

    public HttpServer on(HttpMethod method, String path, HttpHandler handler) {
        router.register(method, path, handler);
        return this;
    }

    public HttpServer get(String path, HttpHandler handler) {
        return on(HttpMethod.GET, path, handler);
    }

    public HttpServer post(String path, HttpHandler handler) {
        return on(HttpMethod.POST, path, handler);
    }

    public HttpServer put(String path, HttpHandler handler) {
        return on(HttpMethod.PUT, path, handler);
    }

    public HttpServer patch(String path, HttpHandler handler) {
        return on(HttpMethod.PATCH, path, handler);
    }

    public HttpServer delete(String path, HttpHandler handler) {
        return on(HttpMethod.DELETE, path, handler);
    }

    public void start() throws IOException {
        if (running) throw new IllegalStateException("Server already running");
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.host(), config.port()));
        serverChannel.configureBlocking(true);

        executor = config.useVirtualThreads()
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(config.threadPoolSize());

        running = true;
        LOGGER.info("HTTP Server started on " + config.host() + ":" + config.port());

        try {
            while (running) {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    executor.submit(new ClientHandler(clientChannel, router));
                }
            }
        } catch (IOException e) {
            if (running) LOGGER.log(Level.SEVERE, "Error accepting connection", e);
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try { if (serverChannel != null && serverChannel.isOpen()) serverChannel.close(); }
        catch (IOException e) { LOGGER.log(Level.WARNING, "Error closing server channel", e); }
        if (executor != null) {
            executor.shutdown();
            try { if (!executor.awaitTermination(10, TimeUnit.SECONDS)) executor.shutdownNow(); }
            catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
        }
        LOGGER.info("HTTP Server stopped");
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 8080;
        private int threadPoolSize = 10;
        private boolean useVirtualThreads = false;

        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder threadPoolSize(int size) { this.threadPoolSize = size; return this; }
        public Builder useVirtualThreads(boolean use) { this.useVirtualThreads = use; return this; }
        public HttpServer build() {
            ServerConfig config = new ServerConfig(host, port, threadPoolSize, useVirtualThreads);
            Router router = new Router();
            return new HttpServer(config, router);
        }
    }

    private static class ClientHandler implements Runnable {
        private final SocketChannel channel;
        private final Router router;

        ClientHandler(SocketChannel channel, Router router) {
            this.channel = channel;
            this.router = router;
            try {
                channel.configureBlocking(true);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException("Failed to configure client channel", e);
            }
        }

        @Override
        public void run() {
            try (channel) {
                System.out.println("Client connected: " + channel.getRemoteAddress());
                boolean keepAlive = true;
                while (keepAlive && channel.isOpen()) {
                    System.out.println("Waiting for request...");
                    HttpRequest request = RequestParser.parse(channel);
                    if (request == null) {
                        System.out.println("No request, closing");
                        break;
                    }
                    System.out.println("Got request: " + request.method() + " " + request.path());
                    HttpResponse response = router.handle(request);
                    System.out.println("Response status: " + response.getStatus());
                    ResponseWriter.write(channel, response);
                    System.out.println("Response sent");
                    keepAlive = "keep-alive".equalsIgnoreCase(request.getHeader("Connection"));
                    if (!keepAlive) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}