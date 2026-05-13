package org.internal;

import org.api.HttpMethod;
import org.api.HttpRequest;
import org.api.HttpResponse;
import org.api.RequestHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Router {

    private final Map<RouteKey, RequestHandler> routes = new ConcurrentHashMap<>();

    public void add(HttpMethod method, String path, RequestHandler handler) {
        routes.put(new RouteKey(method, path), handler);
    }

    public HttpResponse dispatch(HttpRequest request) {
        RouteKey       key     = new RouteKey(request.method(), request.path());
        RequestHandler handler = routes.get(key);

        if (handler != null) {
            try {
                return handler.handle(request);
            } catch (Exception e) {
                return HttpResponse.internalServerError().body("Handler error: " + e.getMessage());
            }
        }

        boolean pathExists = routes.keySet().stream()
                .anyMatch(k -> k.path().equals(request.path()));

        if (pathExists) {
            Set<String> allowed = routes.keySet().stream()
                    .filter(k -> k.path().equals(request.path()))
                    .map(k -> k.method().name())
                    .collect(Collectors.toSet());
            return HttpResponse.methodNotAllowed()
                    .header("Allow", String.join(", ", allowed))
                    .body("Method Not Allowed");
        }

        return HttpResponse.notFound().body("Not Found: " + request.path());
    }
}
