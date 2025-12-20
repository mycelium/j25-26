package analysis.sentiment;

import java.io.*;
import java.util.*;

public class Preparing {

    public enum Sentiment {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    public static class Review {
        private String reviewText;
        private Sentiment reviewSentiment;

        public Review(String str, Sentiment sent) {
            this.reviewText = str;
            this.reviewSentiment = sent;
        }

        public String getText() {
            return reviewText;
        }

        public String getSentiment() {
            return reviewSentiment.name().toLowerCase();
        }
    }

    public List<Review> loadReviews(String filePath) throws IOException {
        List<Review> reviews = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                if (line.isEmpty()) continue;

                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length != 2) continue;

                String reviewText = parts[0].replace("\"", "").trim();
                String sentimentStr = parts[1].replace("\"", "").trim().toLowerCase();

                Sentiment sentiment = setSentiment(sentimentStr);
                if (sentiment == null) continue;

                reviewText = clearTechSymbols(reviewText);
                reviews.add(new Review(reviewText, sentiment));
            }
        }

        return reviews;
    }


    private Sentiment setSentiment(String reviewSentimentString) {
        switch (reviewSentimentString) {
            case "positive":
                return Sentiment.POSITIVE;
            case "neutral":
                return Sentiment.NEUTRAL;
            case "negative":
                return Sentiment.NEGATIVE;
            default:
                return null;
        }
    }

    private String clearTechSymbols(String text) {
        return text.replace("<br />", "").trim();
    }
}
