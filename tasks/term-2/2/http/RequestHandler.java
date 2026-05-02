package http;

public interface RequestHandler {
    void process(HttpRequest req, HttpResponse res) throws Exception;
}