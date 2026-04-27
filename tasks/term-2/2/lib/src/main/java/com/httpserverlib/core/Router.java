package com.httpserverlib.core;

import com.httpserverlib.handler.HttpHandler;
import com.httpserverlib.model.HttpMethod;
import com.httpserverlib.model.HttpRequest;
import com.httpserverlib.model.HttpResponse;
import com.httpserverlib.model.HttpStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Router {
    private final Map<RouteKey, HttpHandler> routes = new ConcurrentHashMap<>();

    public void register(HttpMethod method, String path, HttpHandler handler) {
        routes.put(new RouteKey(method, normalizePath(path)), handler);
    }

    public HttpResponse handle(HttpRequest request) {
        RouteKey key = new RouteKey(request.getMethod(), normalizePath(request.getPath()));
        HttpHandler handler = routes.get(key);
        if (handler == null) {
            boolean pathExists = routes.keySet().stream().anyMatch(k -> k.path().equals(normalizePath(request.getPath())));
            if (pathExists) return new HttpResponse().status(HttpStatus.METHOD_NOT_ALLOWED);
            return new HttpResponse().status(HttpStatus.NOT_FOUND);
        }
        try {
            return handler.handle(request);
        } catch (Exception e) {
            return new HttpResponse().status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        return path;
    }

    private record RouteKey(HttpMethod method, String path) {}
}