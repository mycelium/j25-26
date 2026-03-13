package http;

public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}