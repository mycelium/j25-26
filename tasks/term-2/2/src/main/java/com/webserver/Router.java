package com.webserver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Router {
    private final Map<String, Map<String, BiFunction<Request, Response, Response>>> table = new HashMap<>();

    public void register(String method, String path, BiFunction<Request, Response, Response> handler) {
        table.computeIfAbsent(method.toUpperCase(), k -> new HashMap<>()).put(path, handler);
    }

    public BiFunction<Request, Response, Response> resolve(String method, String path) {
        Map<String, BiFunction<Request, Response, Response>> byMethod = table.get(method.toUpperCase());
        if (byMethod == null) return null;
        return byMethod.get(path);
    }

    public boolean hasRoute(String method, String path) {
        return resolve(method, path) != null;
    }
}
