package org.example;

import java.util.Objects;

public class Review {
    private final int id;
    private final String text;
    private String sentiment;

    public Review(int id, String text) {
        this.id = id;
        this.text = Objects.requireNonNull(text, "Text cannot be null");
        this.sentiment = "NOT_ANALYZED";
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
