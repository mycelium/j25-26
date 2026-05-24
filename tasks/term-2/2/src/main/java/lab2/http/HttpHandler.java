package lab2.http;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
