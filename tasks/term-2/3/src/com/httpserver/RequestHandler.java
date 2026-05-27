package com.httpserver;

@FunctionalInterface
public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
