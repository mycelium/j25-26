package org.example.http;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
