package sentiment.analysis;

public class MovieReview {
    private String reviewText;
    private String sentiment;
    private String actualSentiment;

    public MovieReview (String reviewText) {
        this.reviewText = reviewText;
        this.actualSentiment = "unknown";
    }

    public MovieReview (String reviewText, String sentiment) {
        this.reviewText = reviewText;
        this.sentiment = sentiment;
        this.actualSentiment = "unknown";
    }

    public String getReviewText() { return reviewText; }
    public String getSentiment() { return sentiment; }
    public String getActualSentiment() { return actualSentiment; }

    public void setReviewText (String reviewText) { this.reviewText = reviewText; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public void setActualSentiment(String actualSentiment) {
        this.actualSentiment = actualSentiment;
    }

    @Override
    public String toString() {
        return String.format("Actual: %s | Predicted: %s | Text: %s",
                actualSentiment != null ? actualSentiment : "N/A",
                sentiment != null ? sentiment : "N/A",
                reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText);
    }
}