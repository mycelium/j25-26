package papka_HTTP;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class Server_HTTP {

    private final int port;
    private final String host;
    private final int theadsActiveMax;
    private final boolean isVirtual;

    private ExecutorService executorService;
    private ServerSocketChannel socketChannel;
    private final Map<String, Map<AllMethods, HTTPHandler>> routes;

    private Thread thread;

    private boolean running;

    public Server_HTTP(int vport, String vhost, int vtheadsActiveMax, boolean isVirtual) {
        this.port = vport;
        this.host = vhost;
        this.theadsActiveMax = vtheadsActiveMax;
        this.isVirtual = isVirtual;
        routes  = new HashMap<>();
        running = false;
    }

    public void addHandler(String path, AllMethods method, HTTPHandler handler) {

        Map<AllMethods, HTTPHandler> methodAndHandler = routes.get(path);
        if (methodAndHandler == null) {
            methodAndHandler = new HashMap<>();
            routes.put(path, methodAndHandler);
        }
        methodAndHandler.put(method, handler);
    }

    public void startServer() throws IOException {

        if(running) return;

        socketChannel = ServerSocketChannel.open();
        socketChannel.bind(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(true);

        if (isVirtual) {
            executorService = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executorService = Executors.newFixedThreadPool(theadsActiveMax);
        }
        running = true;

        thread = new Thread(() -> {
            while (running) {
                try {
                    SocketChannel clientChannel = socketChannel.accept();
                    if (clientChannel != null) {
                        executorService.submit(() -> handleClient(clientChannel));
                    }
                } catch (IOException exception) {
                    if(running){
                        System.out.println("Ошибка" + exception);
                    }
                }
            }
        });
        thread.start();
    }

    private void handleClient(SocketChannel client) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            client.read(buffer);
            buffer.flip();

            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            InputStream inpStrm = new ByteArrayInputStream(data);
            HTTPRequest request = HTTPParser.parse(inpStrm);

            if (request == null) {
                client.close();
                return;
            }

            Map<AllMethods, HTTPHandler> handlers = routes.get(request.getPath());
            HTTPResponse response = new HTTPResponse();

            if (handlers != null && handlers.containsKey(request.getMethod())) {
                handlers.get(request.getMethod()).handle(request, response);
            } else {
                response.setStutus(404);
                response.setBody("Not Found");
            }

            byte[] responseBytes = response.toBytes();
            ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
            client.write(responseBuffer);

            client.close();

        } catch (IOException exception) {
            System.out.println("Ошибка" + exception);
            try {
                client.close();
            } catch (IOException ex) {

            }
        }

    }

    public void stopServer() {
        running = false;

        if (executorService != null) {
            executorService.shutdown();
        }
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
        } catch (IOException e) {
            System.out.println("Ошибка остановки");
        }
        System.out.println("Сервер остановлен");
    }




}

