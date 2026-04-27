package com.httpserverlib.handler;

import com.httpserverlib.model.HttpRequest;
import com.httpserverlib.model.HttpResponse;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}