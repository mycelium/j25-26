package com.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class WebServer {
    private final String host;
    private final int port;
    private final int threadPoolSize;
    private final boolean virtualThreads;
    private final Router router;

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean active;

    public WebServer(String host, int port, int threadPoolSize, boolean virtualThreads) {
        this.host = host;
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.virtualThreads = virtualThreads;
        this.router = new Router();
    }

    public WebServer(WebServerConfig config) {
        this(config.getHost(), config.getPort(), config.getThreadPoolSize(), config.isVirtualThreads());
    }

    public WebServer get(String path, BiFunction<Request, Response, Response> handler) {
        router.register("GET", path, handler);
        return this;
    }

    public WebServer post(String path, BiFunction<Request, Response, Response> handler) {
        router.register("POST", path, handler);
        return this;
    }

    public WebServer put(String path, BiFunction<Request, Response, Response> handler) {
        router.register("PUT", path, handler);
        return this;
    }

    public WebServer patch(String path, BiFunction<Request, Response, Response> handler) {
        router.register("PATCH", path, handler);
        return this;
    }

    public WebServer delete(String path, BiFunction<Request, Response, Response> handler) {
        router.register("DELETE", path, handler);
        return this;
    }

    public void start() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(host, port));

            executor = virtualThreads
                    ? Executors.newVirtualThreadPerTaskExecutor()
                    : Executors.newFixedThreadPool(threadPoolSize);

            active = true;
            System.out.printf("WebServer listening on %s:%d (virtualThreads=%b, poolSize=%d)%n",
                    host, port, virtualThreads, threadPoolSize);

            acceptLoop();
        } catch (IOException e) {
            throw new WebServerException("Server startup failed", e);
        }
    }

    private void acceptLoop() throws IOException {
        while (active) {
            try {
                SocketChannel client = serverChannel.accept();
                executor.execute(() -> handleConnection(client));
            } catch (ClosedChannelException e) {
                if (active) throw e;
                break;
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        try (SocketChannel ch = channel;
             InputStream in = Channels.newInputStream(ch);
             OutputStream out = Channels.newOutputStream(ch)) {

            Request request = RequestParser.parse(in);
            if (request == null) return;

            Response response = dispatch(request);
            out.write(response.serialize());
            out.flush();

        } catch (IOException ignored) {
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private Response dispatch(Request request) {
        Response response = new Response();
        BiFunction<Request, Response, Response> handler =
                router.resolve(request.getMethod(), request.getPath());

        if (handler != null) {
            return handler.apply(request, response);
        }
        return response.status(404, "Not Found").body("404 - Not Found");
    }

    public void stop() {
        active = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException ignored) {
        }
        if (executor != null) executor.shutdown();
    }
}
