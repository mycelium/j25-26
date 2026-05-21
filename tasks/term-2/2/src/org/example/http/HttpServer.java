package org.example.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final String host;
    private final int port;
    private final int threadPoolSize;
    private final boolean isVirtual;
    
    // Routes: Path -> (Method -> Handler)
    private final Map<String, Map<HttpMethod, HttpHandler>> routes = new HashMap<>();
    
    private ExecutorService executorService;
    private ServerSocketChannel serverChannel;
    private volatile boolean isRunning = false;

    /**
     * Конструктор сервера
     * @param host Хост (например, "localhost")
     * @param port Порт
     * @param threadPoolSize Размер пула потоков
     * @param isVirtual Если true, использует Virtual Threads (Java 21+), иначе фиксированный пул
     */
    public HttpServer(String host, int port, int threadPoolSize, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.isVirtual = isVirtual;
    }

    
    
    public void addRoute(String path, HttpMethod method, HttpHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
    }

    
    public void start() throws IOException {
        if (isRunning) return;
        
        
        if (isVirtual) {
            try {
                
                executorService = Executors.newVirtualThreadPerTaskExecutor();
                System.out.println("Started with Virtual Threads");
            } catch (NoSuchMethodError e) {
                System.err.println("Virtual threads not supported on this JVM. Falling back to platform threads.");
                executorService = Executors.newFixedThreadPool(threadPoolSize);
            }
        } else {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            System.out.println("Started with Fixed Thread Pool: " + threadPoolSize);
        }

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        
        serverChannel.configureBlocking(true); 
        
        isRunning = true;
        System.out.println("Server listening on " + host + ":" + port);

        while (isRunning) {
            try {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    
                    executorService.submit(new ConnectionHandler(clientChannel, routes));
                }
            } catch (IOException e) {
                if (isRunning) e.printStackTrace();
            }
        }
    }

    
    public void stop() {
        isRunning = false;
        try {
            if (serverChannel != null) serverChannel.close();
            if (executorService != null) executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped.");
    }
}