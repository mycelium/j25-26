package httpserver;

@FunctionalInterface
public interface RequestHandler {
    Response handle(Request request) throws Exception;
}
