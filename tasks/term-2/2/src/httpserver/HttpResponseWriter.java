package httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

final class HttpResponseWriter {
    private HttpResponseWriter() {
    }

    static void write(SocketChannel socketChannel, HttpResponse response) throws IOException {
        byte[] body = response.getBody();
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            String lowerCaseName = entry.getKey().toLowerCase();
            if (!"content-length".equals(lowerCaseName) && !"connection".equals(lowerCaseName)) {
                headers.put(entry.getKey(), entry.getValue());
            }
        }
        headers.put("Content-Length", String.valueOf(body.length));
        headers.put("Connection", "close");

        try (OutputStream outputStream = Channels.newOutputStream(socketChannel)) {
            StringBuilder responseHead = new StringBuilder();
            responseHead.append("HTTP/1.1 ")
                    .append(response.getStatusCode())
                    .append(' ')
                    .append(reasonPhrase(response.getStatusCode()))
                    .append("\r\n");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                responseHead.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
            }
            responseHead.append("\r\n");

            outputStream.write(responseHead.toString().getBytes(StandardCharsets.ISO_8859_1));
            outputStream.write(body);
            outputStream.flush();
        }
    }

    private static String reasonPhrase(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            default -> "Status";
        };
    }
}
