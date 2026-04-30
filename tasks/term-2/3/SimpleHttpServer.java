import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class SimpleHttpServer {
    private final String host;
    private final int port;
    private final int threadCount;
    private final boolean isVirtual;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;
    private volatile boolean running = false;

    private final Map<String, Map<String, HttpHandler>> handlers = new HashMap<>();

    public SimpleHttpServer(String host, int port, int threadCount, boolean isVirtual) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
        this.isVirtual = isVirtual;

        handlers.put("GET", new HashMap<>());
        handlers.put("POST", new HashMap<>());
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);

        if (isVirtual) {
            executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            executor = Executors.newFixedThreadPool(threadCount);
        }

        running = true;

        while (running) {
            try {
                SocketChannel client = serverChannel.accept();
                executor.submit(() -> handleClient(client));
            } catch (ClosedChannelException e) {
                if (running) e.printStackTrace();
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }

    public void post(String path, HttpHandler handler) {
        handlers.get("POST").put(path, handler);
    }

    public void get(String path, HttpHandler handler) {
        handlers.get("GET").put(path, handler);
    }

    private void handleClient(SocketChannel clientChannel) {
        try (clientChannel) {
            InputStream in = clientChannel.socket().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            HttpRequest request = parseRequestLine(requestLine);
            Map<String, String> headers = parseHeaders(reader);
            request.setHeaders(headers);
            String body = parseBody(reader, headers);
            request.setBody(body);

            HttpResponse response = new HttpResponse();
            HttpHandler handler = handlers.getOrDefault(request.getMethod(), new HashMap<>())
                                          .get(request.getPath());

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    response.status(500);
                    response.send("Error: " + e.getMessage());
                }
            } else {
                response.status(404);
                response.send("Not Found");
            }

            sendResponse(clientChannel, response);
        } catch (IOException e) {
            // ignore
        }
    }

    private HttpRequest parseRequestLine(String requestLine) {
        HttpRequest req = new HttpRequest();
        String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
            req.setMethod(parts[0]);
            req.setPath(parts[1].split("\\?")[0]);
        }
        return req;
    }

    private Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                headers.put(parts[0].toLowerCase().trim(), parts[1].trim());
            }
        }
        return headers;
    }

    private String parseBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        String contentLengthStr = headers.get("content-length");
        if (contentLengthStr != null) {
            int len = Integer.parseInt(contentLengthStr);
            if (len > 0) {
                char[] buf = new char[len];
                int read = 0;
                while (read < len) {
                    int r = reader.read(buf, read, len - read);
                    if (r == -1) break;
                    read += r;
                }
                return new String(buf, 0, read);
            }
        }
        return "";
    }

    private void sendResponse(SocketChannel channel, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append("\r\n");
        for (Map.Entry<String, String> h : response.getHeaders().entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        sb.append("\r\n").append(response.getBody());

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        while (buf.hasRemaining()) channel.write(buf);
    }
}
