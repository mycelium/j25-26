import httpserver.HttpMethod;
import httpserver.HttpResponse;
import httpserver.HttpServer;
import httpserver.MultipartPart;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Main {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8080;
        int threadCount = 4;
        boolean isVirtual = true;

        HttpServer server = new HttpServer(host, port, threadCount, isVirtual);

        server.addListener(HttpMethod.GET, "/hello", request -> {
            String name = request.getQueryParams().getOrDefault("name", "guest");
            String clientName = request.getHeaders().getOrDefault("x-client-name", "unknown");

            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "text/plain; charset=UTF-8");
            headers.put("X-Handled-By", "GET /hello");

            String body = "Hello, " + name + "!\n"
                    + "method=" + request.getMethod() + "\n"
                    + "path=" + request.getPath() + "\n"
                    + "x-client-name=" + clientName;

            return new HttpResponse(200, headers, body.getBytes(StandardCharsets.UTF_8));
        });

        server.addListener(HttpMethod.POST, "/echo", request ->
                HttpResponse.text(200, "POST body:\n" + request.getBodyAsString()));

        server.addListener(HttpMethod.PUT, "/resource", request ->
                HttpResponse.text(200, "PUT body:\n" + request.getBodyAsString()));

        server.addListener(HttpMethod.PATCH, "/resource", request ->
                HttpResponse.text(200, "PATCH body:\n" + request.getBodyAsString()));

        server.addListener(HttpMethod.DELETE, "/resource", request ->
                HttpResponse.text(200, "Resource deleted"));

        server.addListener(HttpMethod.POST, "/upload", request -> {
            List<MultipartPart> parts = request.getMultipartParts();
            StringBuilder body = new StringBuilder("Multipart parts:\n");

            for (MultipartPart part : parts) {
                body.append("name=").append(part.getName())
                        .append(", filename=").append(part.getFilename())
                        .append(", content=").append(part.getContentAsString())
                        .append('\n');
            }

            return HttpResponse.text(200, body.toString().trim());
        });

        server.start();

        System.out.println("HTTP server example is running.");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Thread count: " + threadCount);
        System.out.println("Virtual executor: " + isVirtual);
        System.out.println();
        System.out.println("Example requests:");
        System.out.println("GET    http://127.0.0.1:8080/hello?name=Bob");
        System.out.println("       Header: X-Client-Name: demo");
        System.out.println("POST   http://127.0.0.1:8080/echo");
        System.out.println("       Body: any text");
        System.out.println("PUT    http://127.0.0.1:8080/resource");
        System.out.println("       Body: full resource representation");
        System.out.println("PATCH  http://127.0.0.1:8080/resource");
        System.out.println("       Body: partial resource update");
        System.out.println("DELETE http://127.0.0.1:8080/resource");
        System.out.println("POST   http://127.0.0.1:8080/upload");
        System.out.println("       Content-Type: multipart/form-data");
        System.out.println();
        System.out.println("Press Enter to stop the server.");

        try {
            System.in.read();
        } finally {
            server.stop();
        }
    }
}
