package httpserver;

@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
