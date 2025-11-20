package org.example;

public class MovieReview {
    private String reviewText;
    private String sentiment;

    public MovieReview (String reviewText) {
        this.reviewText = reviewText;
    }

    public String getReviewText() { return reviewText; }
    public String getSentiment() { return sentiment; }

    public void setReviewText (String reviewText) { this.reviewText = reviewText; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    @Override
    public String toString() {
        return String.format("Sentiment: %s | Text: %s",
                sentiment,
                reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText);
    }
}
