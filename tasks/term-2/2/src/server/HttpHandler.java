package server;

@FunctionalInterface
public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response);
}
