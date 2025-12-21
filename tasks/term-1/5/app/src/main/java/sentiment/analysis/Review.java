package sentiment.analysis;

import java.util.Objects;

public class Review {
    private final String text;
    private final String actualSentiment;
    private String calculatedSentiment;


    public Review(String text, String actualSentiment) {
        this.text = Objects.requireNonNull(text, "Text cannot be null");
        this.actualSentiment = Objects.requireNonNull(actualSentiment, "Actual sentiment cannot be null").toLowerCase();
        this.calculatedSentiment = "NOT_ANALYZED";
    }

    public String getText() {
        return text;
    }

    public String getActualSentiment() {
        return actualSentiment;
    }

    public String getCalculatedSentiment() {
        return calculatedSentiment;
    }

    public void setCalculatedSentiment(String sentiment) {
        this.calculatedSentiment = sentiment;
    }
}

