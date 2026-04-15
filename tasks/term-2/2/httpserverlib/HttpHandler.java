package httpserverlib;

public interface HttpHandler {
    void handle(Request request, Response response) throws Exception;
}