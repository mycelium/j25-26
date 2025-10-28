package org.example;

import java.io.*;
import java.util.*;

public class ReviewProcessor {
    private SentimentAnalyzer analyzer;

    public ReviewProcessor() {
        this.analyzer = new SentimentAnalyzer();
    }

    public List<MovieReview> processReviews(String filename) {
        List<MovieReview> reviews = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int id = 1;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                MovieReview review = new MovieReview("R" + id++, line);
                String sentiment = analyzer.analyzeSentiment(line);
                review.setSentiment(sentiment);
                reviews.add(review);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return reviews;
    }
}