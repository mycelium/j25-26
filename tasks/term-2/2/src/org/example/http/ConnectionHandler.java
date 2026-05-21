package org.example.http;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class ConnectionHandler implements Runnable {
    private final SocketChannel channel;
    private final Map<String, Map<HttpMethod, HttpHandler>> routes;

    public ConnectionHandler(SocketChannel channel, Map<String, Map<HttpMethod, HttpHandler>> routes) {
        this.channel = channel;
        this.routes = routes;
    }

    @Override
    public void run() {
        try {
            RequestParser parser = new RequestParser();
            HttpRequest request = parser.parse(channel);

            if (request == null) {
                channel.close();
                return;
            }

            HttpResponse response = new HttpResponse();
            
            // Routing logic
            Map<HttpMethod, HttpHandler> methodHandlers = routes.get(request.getPath());
            HttpHandler handler = null;
            
            if (methodHandlers != null) {
                handler = methodHandlers.get(request.getMethod());
            }

            if (handler != null) {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    response.setStatus(500, "Internal Server Error");
                    response.setBody("Error processing request: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                response.setStatus(404, "Not Found");
                response.setBody("Path or Method not found: " + request.getMethod() + " " + request.getPath());
            }

            channel.write(java.nio.ByteBuffer.wrap(response.toBytes()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (Exception ignored) {}
        }
    }
}