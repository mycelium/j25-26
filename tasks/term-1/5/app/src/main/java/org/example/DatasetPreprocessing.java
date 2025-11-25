package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DatasetPreprocessing {
    
    public List<MovieReview> loadDataset(String filePath) throws IOException {
        List<MovieReview> reviews = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        System.out.println("Loading dataset from: " + filePath);
        System.out.println("Total lines in file: " + lines.size());
        
        for (int i = 1; i < lines.size(); i++) { 
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 2) {
                String reviewText = parts[0].replace("\"", "").trim();
                String sentiment = parts[1].replace("\"", "").trim();
                
                reviewText = cleanText(reviewText);
                reviews.add(new MovieReview(reviewText, sentiment));
            }
            
            if (i % 1000 == 0) {
                System.out.println("Loaded " + i + " reviews...");
            }
        }
        System.out.println("Successfully loaded " + reviews.size() + " reviews");
        return reviews;
    }
    
    private String cleanText(String text) {
        return text.replace("<br />", " ")
                   .replaceAll("[^a-zA-Z0-9\\s.,!?]", "")
                   .trim();
    }
}