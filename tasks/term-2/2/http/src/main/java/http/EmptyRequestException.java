package http;

import java.io.IOException;


public class EmptyRequestException extends IOException {
    public EmptyRequestException() {
        super("Empty request (client closed connection)");
    }
}