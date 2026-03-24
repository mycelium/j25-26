@FunctionalInterface
public interface Handler {
    void handle(Request request, Response response) throws Exception;
}