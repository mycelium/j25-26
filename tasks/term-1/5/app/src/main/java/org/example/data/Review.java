package org.example.data;

public class Review {

    private final String text;
    private final String sentiment;

    public Review(String text, String sentiment) {
        this.text = text;
        this.sentiment = sentiment;
    }

    public String getText() {
        return text;
    }

    public String getSentiment() {
        return sentiment;
    }

}
