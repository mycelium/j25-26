package http;

@FunctionalInterface
public interface RouteHandler {
    void handle(HttpRequest request, HttpResponse response) throws Exception;
}
