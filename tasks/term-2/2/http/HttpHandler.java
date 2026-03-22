package http;

public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
