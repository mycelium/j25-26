package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Preprocessing {
    
    public List<Review> loadDataset(String filePath) throws IOException {
        List<Review> reviews = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 2) {
                String reviewText = parts[0].replace("\"", "").trim();
                String sentiment = parts[1].replace("\"", "").trim();
                
                reviewText = cleanText(reviewText);
                
                reviews.add(new Review(reviewText, sentiment));
            }
        }
        return reviews;
    }
    
    private String cleanText(String text) {
        return text.replace("<br />", " ").trim();
    }
    
    public static class Review {
        private String text;
        private String actualSentiment;
        
        public Review(String text, String actualSentiment) {
            this.text = text;
            this.actualSentiment = actualSentiment;
        }
        
        public String getText() { return text; }
        public String getActualSentiment() { return actualSentiment; }
    }
}