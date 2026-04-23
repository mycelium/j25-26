package lab2.http;

@FunctionalInterface
public interface HttpRouteHandler {
    ServerResponse execute(IncomingRequest req) throws Exception;
}