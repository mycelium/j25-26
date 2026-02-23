package org.example;

import java.io.Serializable;

public class Response implements Serializable {
    
    private String sentiment;
    private boolean success;

    public Response(String sentiment, boolean success) {
        this.sentiment = sentiment;
        this.success = success;
    }

    public String getSentiment() {
        return sentiment;
    }

    public boolean isSuccess() {
        return success;
    }
}