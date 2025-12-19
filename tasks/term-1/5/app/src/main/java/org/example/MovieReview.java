package org.example;

public class MovieReview {
    private final String id;
    private final String text;
    private String sentiment;

    public MovieReview(String id, String text) {
        this.id = id != null ? id.trim() : "UNKNOWN";
        this.text = text != null ? text.trim() : "";
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public String getSentiment() { return sentiment != null ? sentiment : "neutral"; }
    public void setSentiment(String sentiment) {
        this.sentiment = (sentiment != null) ? sentiment.toLowerCase().trim() : "neutral";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s", id, getSentiment().toUpperCase(), text);
    }
}
