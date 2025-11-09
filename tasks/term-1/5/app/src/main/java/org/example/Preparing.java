package org.example;

import java.io.*;
import java.nio.file.*;
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
            return reviewSentiment.toString().toLowerCase();
        }

    }

    public List<Review> loadReviews(String filePath) throws IOException {
        List<Review> reviews = new ArrayList<>();
        List<String> stringLines = Files.readAllLines(Paths.get(filePath));

        for (int i = 1; i < stringLines.size(); i++) {
            String line = stringLines.get(i);

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(".\",");
            if (parts.length == 2) {
                String reviewText = parts[0].replace("\"", "").trim();
                String reviewSentimentString = parts[1].trim();
                Sentiment reviewSentiment = setSentiment(reviewSentimentString);
                reviewText = clearTechSymbols(reviewText);

                reviews.add(new Review(reviewText, reviewSentiment));
            }
            
        }

        return reviews;
    }

    private Sentiment setSentiment(String reviewSentimentString) {
        Sentiment result;
        switch (reviewSentimentString) {
            case "positive":
                result = Sentiment.POSITIVE;
                break;
        
            case "neutral":
                result = Sentiment.NEUTRAL;
                break;

            case "negative":
                result = Sentiment.NEGATIVE;
                break;

            default:
                result = null;    
                break;
        }

        return result;
    }

    private String clearTechSymbols(String text) {
        return text.replace("<br />", "").trim();
    }


}