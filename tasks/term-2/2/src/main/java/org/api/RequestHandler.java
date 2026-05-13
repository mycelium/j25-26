package org.api;

@FunctionalInterface
public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
