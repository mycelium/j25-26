package httpserver;

@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res);
}