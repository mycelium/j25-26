package org.example;

import java.util.*;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        try {
            ReviewAnalyser analyser = new ReviewAnalyser();
            String filePath = "IMDB Dataset.csv";
            List<ReviewAnalyser.Review> reviews = analyser.loadReviews(filePath);
            System.out.println("Analyse reviews");
            int limit = Math.min(30, reviews.size());
            for (int i = 0; i < limit; i++) {
                ReviewAnalyser.Review review = reviews.get(i);
                String predictedSentiment = analyser.Analyse(review.text());
                String truncatedText = review.text().length() > 50
                        ? review.text().substring(0, 47) + "..."
                        : review.text();
                System.out.printf("%-5d %-50s %-15s %-15s %n",
                        i + 1,
                        truncatedText,
                        review.actualSentiment(),
                        predictedSentiment
                );
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
