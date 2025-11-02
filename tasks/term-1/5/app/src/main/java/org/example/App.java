package org.example;

import java.util.*;

public class App {
    public static void main(String[] args) {
        try {
            String datasetPath = "j25-26\\tasks\\term-1\\5\\app\\src\\main\\resources\\dataset\\IMDB Dataset.csv";
            Preprocessing preprocessor = new Preprocessing();
            List<Preprocessing.Review> reviews = preprocessor.loadDataset(datasetPath);

            SentimentAnalyzer analyzer = new SentimentAnalyzer();

            System.out.println("---===[Sentiment Analysis Results]===---");
            
            int correct = 0;
            int totalToShow = 20;
            
            for (int i = 0; i < totalToShow; i++) {
                int min = 0;
                int max = reviews.size()-1;
                int randomInRange = min + (int) (Math.random() * ((max - min) + 1));

                Preprocessing.Review review = reviews.get(randomInRange);
                String predictedSentiment = analyzer.analyzeSentiment(review.getText());
                
                System.out.println("Review " + (i + 1) + ":");
                System.out.println("Text: " + (
                    review.getText().length() > 300 ? 
                    review.getText().substring(0, 300) + "..." :
                    review.getText()
                ));
                System.out.println("Actual: " + review.getActualSentiment() + " | Predicted: " + predictedSentiment);
                System.out.println("---");
                
                if (review.getActualSentiment().equalsIgnoreCase(predictedSentiment)) {
                    correct++;
                }
            }
            
            System.out.println("Accuracy on sample: " + String.format("%.2f", (correct * 100.0 / totalToShow)) + "%");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}