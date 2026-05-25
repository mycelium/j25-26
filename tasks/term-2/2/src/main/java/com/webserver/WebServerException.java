package com.webserver;

public class WebServerException extends RuntimeException {
    public WebServerException(String message) {
        super(message);
    }

    public WebServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
