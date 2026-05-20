package lab2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Worker implements Runnable {
    
    private SocketChannel channel;
    private HttpServer server;
    
    public Worker(SocketChannel channel, HttpServer server) {
        this.channel = channel;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            Request request = readRequest();
            Response response = new Response();
            server.routeRequest(request, response);
            sendResponse(response);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
            }
        }
    }

    private Request readRequest() throws IOException {
        StringBuilder rawRequest = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        
        while (true) {
            int bytesRead = channel.read(buffer);
            if (bytesRead <= 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                rawRequest.append((char) buffer.get());
            }
            buffer.clear();
            String headersEnd = "\r\n\r\n";
            int headersIndex = rawRequest.indexOf(headersEnd);
            if (headersIndex >= 0) {
                String headersPart = rawRequest.substring(0, headersIndex);
                int contentLength = parseContentLength(headersPart);
                int bodyStart = headersIndex + headersEnd.length();
                int bodyRead = rawRequest.length() - bodyStart;
                if (contentLength > 0 && bodyRead < contentLength) {
                    continue;
                }
                break;
            }
        }
        return parseRequest(rawRequest.toString());
    }
    
    private int parseContentLength(String headers) {
        String[] lines = headers.split("\r\n");
        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.startsWith("content-length:")) {
                try {
                    return Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
    
    private Request parseRequest(String raw) {
        Request request = new Request();
        String[] parts = raw.split("\r\n\r\n", 2);
        String headerPart = parts[0];
        String body = (parts.length > 1) ? parts[1] : "";
        
        String[] lines = headerPart.split("\r\n");
        if (lines.length == 0) return request;
        String[] firstLine = lines[0].split(" ");
        if (firstLine.length >= 2) {
            request.setMethod(firstLine[0]);
            request.setPath(firstLine[1]);
        }
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                request.addHeader(name, value);
            }
        }
        request.setBody(body);
        return request;
    }

    private void sendResponse(Response response) throws IOException {
        String responseString = response.toHttpString();
        byte[] bytes = responseString.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}