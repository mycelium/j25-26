package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReviewProcessor {
    private final SentimentAnalyzer analyzer;

    public ReviewProcessor() {
        this.analyzer = new SentimentAnalyzer();
    }

    public List<MovieReview> processReviews(String filename) {
    List<MovieReview> reviews = new ArrayList<>();
    Path path = Path.of(filename);

    if (!Files.exists(path)) {
        System.err.println("  File not found: " + filename);
        return reviews;
    }

    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        String line;
        int id = 1;

        while ((line = reader.readLine()) != null && reviews.size() < 10) {  
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue;
            }

            MovieReview review = new MovieReview("R" + id++, line);
            String sentiment = analyzer.analyzeSentiment(line);
            review.setSentiment(sentiment);
            reviews.add(review);
        }

        if (reviews.size() < 15) {
            System.out.println("  Processed only " + reviews.size() + " reviews (file ended or too few valid lines).");
        } else {
            System.out.println(" Processed first 15 reviews.");
        }

    } catch (IOException e) {
        System.err.println(" I/O error reading '" + filename + "': " + e.getMessage());
    }

    return reviews;
}
}
