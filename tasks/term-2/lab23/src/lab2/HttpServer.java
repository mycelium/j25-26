package lab2;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    
    private String host;
    private int port;
    private int threadPoolSize;
    private boolean isVirtual;
    private ExecutorService executor;
    private ServerSocketChannel serverChannel;
    private volatile boolean running;
    private Map<String, Handler> routes;
    
    public HttpServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.threadPoolSize = 10;
        this.isVirtual = false;
        this.routes = new HashMap<>();
        this.running = false;
    }

    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }
    public void setVirtualThreads(boolean isVirtual) {
        this.isVirtual = isVirtual;
    }
    public void get(String path, Handler handler) {
        addRoute("GET", path, handler);
    }
    public void post(String path, Handler handler) {
        addRoute("POST", path, handler);
    }
    public void put(String path, Handler handler) {
        addRoute("PUT", path, handler);
    }
    public void delete(String path, Handler handler) {
        addRoute("DELETE", path, handler);
    }
    private void addRoute(String method, String path, Handler handler) {
        String key = method + ":" + path;
        routes.put(key, handler);
    }
    
    public void start() throws IOException {
        if (isVirtual) {
            try {
                executor = Executors.newVirtualThreadPerTaskExecutor();
            } catch (Exception e) {
                executor = Executors.newFixedThreadPool(threadPoolSize);
            }
        } else {
            executor = Executors.newFixedThreadPool(threadPoolSize);
        }
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        running = true;
        System.out.println("сервер запущен на " + host + ":" + port);
        while (running) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    executor.submit(new Worker(clientChannel, this));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
            if (executor != null) {
                executor.shutdown();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("сервер закрыт");
    }
    
    public void routeRequest(Request request, Response response) {
        String key = request.getMethod() + ":" + request.getPath();
        Handler handler = routes.get(key);
        
        if (handler != null) {
            try {
                handler.handle(request, response);
            } catch (Exception e) {
                response.setStatus(500);
                response.setBody("Internal Server Error: " + e.getMessage());
            }
        } else {
            response.setStatus(404);
            response.setBody("Not Found: " + request.getPath());
        }
    }
}