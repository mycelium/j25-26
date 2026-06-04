package httpserver;

public interface HttpHandler {
    void handle(HttpRequest request, HttpResponse response) throws Exception;
}