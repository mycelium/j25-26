package ru.task2.http;

public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
