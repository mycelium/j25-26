import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    public enum Method {
        GET, POST, PUT, PATCH, DELETE
    }

    private final String host;
    private final int port;
    private final int nThreads;
    private final boolean isVirtual;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running;
    private final Map<String, Map<Method, Handler>> handlers = new HashMap<>();

    public Server(String host, int port) {
        this(host, port, 10, false);
    }

    public Server(String host, int port, int nThreads, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.nThreads = nThreads;
        this.isVirtual = isVirtual;
    }

    public void addHandler(Method method, String path, Handler handler) {
        handlers.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);
        running = true;

        executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(nThreads);

        Thread acceptor = new Thread(() -> {
            while (running) {
                try {
                    SocketChannel client = serverChannel.accept();
                    if (client != null) {
                        executor.submit(() -> handleClient(client));
                    }
                } catch (IOException e) {
                    if (running) System.err.println("Accept error: " + e.getMessage());
                }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            Request request = Request.parse(client.socket().getInputStream());
            Response response = new Response();

            Map<Method, Handler> pathHandlers = handlers.get(request.getPath());
            Handler handler = (pathHandlers != null) ? pathHandlers.get(request.getMethod()) : null;
            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    response.setStatus(500);
                    response.setBody("Internal Server Error");
                }
            } else {
                response.setStatus(404);
                response.setBody("Not Found");
            }

            response.send(client.socket().getOutputStream());
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            // ignore
        }
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}