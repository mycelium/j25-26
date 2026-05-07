package httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final String host;
    private final int port;
    private final int threadPoolSize;
    private final boolean isVirtual;
    private final Map<String, Map<RequestMethod, RouteHandler>> routes = new ConcurrentHashMap<>();
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService executorService;
    private volatile boolean running;

    public HttpServer(String host, int port, int threadPoolSize, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.isVirtual = isVirtual;
    }

    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpRequest request, HttpResponse response);
    }

    public HttpServer addRoute(String path, RequestMethod method, RouteHandler handler) {
        routes.computeIfAbsent(path, k -> new ConcurrentHashMap<>()).put(method, handler);
        return this;
    }

    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        serverSocketChannel.configureBlocking(true);
        running = true;

        executorService = isVirtual ?
                Executors.newVirtualThreadPerTaskExecutor() :
                Executors.newFixedThreadPool(threadPoolSize);

        System.out.println("Server started on " + host + ":" + port);

        while (running) {
            try {
                SocketChannel clientChannel = serverSocketChannel.accept();
                executorService.submit(() -> handleClient(clientChannel));
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    private void handleClient(SocketChannel clientChannel) {
        try {
            HttpRequest request = RequestParser.parse(clientChannel);
            HttpResponse response = new HttpResponse();

            if (request != null) {
                Map<RequestMethod, RouteHandler> pathRoutes = routes.get(request.getPath());
                RouteHandler handler = pathRoutes != null ? pathRoutes.get(request.getMethod()) : null;

                if (handler != null) {
                    handler.handle(request, response);
                } else {
                    response.setStatus(404, "Not Found");
                    response.setBody("404 Not Found");
                }
            } else {
                response.setStatus(400, "Bad Request");
                response.setBody("400 Bad Request");
            }

            ResponseWriter.write(clientChannel, response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}