package httpserver;

public interface HttpHandler {
    void handle(Request request, Response response) throws Exception;
}
