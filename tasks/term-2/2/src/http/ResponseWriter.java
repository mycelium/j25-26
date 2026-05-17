package http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class ResponseWriter {

    private ResponseWriter() {
    }

    static void write(OutputStream out, HttpResponse response) throws IOException {
        byte[] body = response.body();

        if (body.length > 0) {
            response.headers().putIfAbsent("Content-Length", String.valueOf(body.length));
        } else {
            response.headers().putIfAbsent("Content-Length", "0");
        }

        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ")
            .append(response.statusCode())
            .append(' ')
            .append(reasonOf(response.statusCode()))
            .append("\r\n");

        for (Map.Entry<String, String> h : response.headers().entrySet()) {
            head.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
        }
        head.append("\r\n");

        out.write(head.toString().getBytes(StandardCharsets.UTF_8));
        if (body.length > 0) {
            out.write(body);
        }
        out.flush();
    }

    private static String reasonOf(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "OK";
        };
    }
}
