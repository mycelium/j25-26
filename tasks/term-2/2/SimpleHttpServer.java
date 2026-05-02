import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class SimpleHttpServer {
    private final String host;
    private final int port;
    private ExecutorService executorService; 
    private final Map<String, Map<String, RequestHandler>> routes = new HashMap<>();
    private ServerSocketChannel serverSocketChannel;

    public SimpleHttpServer(String host, int port, int threadCount, boolean useVirtualThreads) {
        this.host = host;
        this.port = port;
        
        if (useVirtualThreads) {
            try {

                this.executorService = (ExecutorService) Executors.class
                    .getMethod("newVirtualThreadPerTaskExecutor")
                    .invoke(null);
                System.out.println("Using Virtual Threads (Java 21+)");
            } catch (Exception e) {
                System.out.println("Virtual threads not supported (Java < 21). Falling back to Fixed Thread Pool.");
                this.executorService = Executors.newFixedThreadPool(threadCount);
            }
        } else {
            this.executorService = Executors.newFixedThreadPool(threadCount);
            System.out.println("Using Fixed Thread Pool (" + threadCount + " threads)");
        }
    }

    public void route(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>())
              .put(method.toUpperCase(), handler);
    }

    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        System.out.println("Server listening on http://" + host + ":" + port);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                SocketChannel clientChannel = serverSocketChannel.accept();
                executorService.submit(() -> handleClient(clientChannel));
            }
        } catch (ClosedChannelException e) {
        } finally {
            stop();
        }
    }

    public void stop() throws IOException {
        executorService.shutdown();
        if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
        System.out.println("Server stopped.");
    }

    private void handleClient(SocketChannel clientChannel) {
    try (clientChannel; 
         InputStream inputStream = Channels.newInputStream(clientChannel);
         OutputStream outputStream = Channels.newOutputStream(clientChannel)) {

        HttpRequest request = new HttpRequest(inputStream);
        HttpResponse response = new HttpResponse();

            Map<String, RequestHandler> methodsMap = routes.get(request.getPath());
            RequestHandler handler = (methodsMap != null) ? methodsMap.get(request.getMethod()) : null;

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    System.err.println("Error in handler: " + e.getMessage());
                    e.printStackTrace();
                    response.setStatus(500, "Internal Server Error");
                    response.setBody("500 Internal Server Error: " + e.getMessage());
                }
            } else {
                response.setStatus(404, "Not Found");
                response.setBody("404 Page Not Found");
            }

            response.send(outputStream);

        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
