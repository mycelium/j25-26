package org.example;

public class MovieReview {
    private String id;
    private String text;
    private String sentiment;

    public MovieReview(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    @Override
    public String toString() {
        return String.format("%s → %s", sentiment.toUpperCase(), text);
    }
}