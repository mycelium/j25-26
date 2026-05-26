package com.httpserver;

public class WebServerError extends RuntimeException {
    public WebServerError(String msg) {
        super(msg);
    }

    public WebServerError(String msg, Throwable cause) {
        super(msg, cause);
    }
}