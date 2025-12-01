package org.example;

import java.io.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        
        try {
            File file = new File("data/reviews.csv");
            Scanner scanner = new Scanner(file);
            ArrayList<String> allReviews = new ArrayList<>();
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    String reviewText = parts[0].replace("\"", "");
                    allReviews.add(reviewText);
                }
            }
            scanner.close();
            Collections.shuffle(allReviews);
            SentimentAnalyzer analyzer = new SentimentAnalyzer();
            System.out.println("10 Random Movie Reviews with Sentiment Analysis:\n");
            int count = Math.min(10, allReviews.size());
            for (int i = 0; i < count; i++) {
                String review = allReviews.get(i);
                String shortReview;
                if (review.length() > 70) {
                    shortReview = review.substring(0, 70) + "...";
                } else {
                    shortReview = review;
                }
                String sentiment = analyzer.analyze(review);
                System.out.println((i + 1) + ". " + shortReview + " → " + sentiment);
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка: файл не найден!");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }
}
