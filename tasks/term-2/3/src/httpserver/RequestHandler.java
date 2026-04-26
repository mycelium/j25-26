package httpserver;

public interface RequestHandler {
    Response handle(Request request) throws Exception;
}
