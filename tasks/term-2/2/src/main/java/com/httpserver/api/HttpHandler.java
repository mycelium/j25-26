package com.httpserver.api;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}