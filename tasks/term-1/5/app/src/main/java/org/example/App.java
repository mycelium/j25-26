package org.example;

import org.example.Preparing.*;
import org.example.SentimentAnalyzer.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        try {
            String filePath = "app/src/resources/IMDB Dataset.csv";
            Preparing prepair = new Preparing();
            List<Review> reviews = prepair.loadReviews(filePath); 
            SentimentAnalyzer analyzer = new SentimentAnalyzer();

            System.out.println("------------- Tests -------------");


            int correctPredicts = 0;
            int numTests = 20;
            int startIndex = 0;
            for (int i = startIndex; i < startIndex + numTests; i++) {
                
                System.out.println("Review" + (i + 1) +  ": ");
                Review review = reviews.get(i);

                String predict = analyzer.sentimentAnalyze(review.getText());
                Boolean flag = review.getSentiment().equalsIgnoreCase(predict);

                System.out.println("Predict result: " + predict);
                System.out.println("Correct result: " + flag);

                if (flag.equals(true)) {
                    correctPredicts++;
                }

                System.out.println();
            }

            System.out.println("\n------------- Summary -------------");

            System.out.println("Total predicts: " + numTests);
            System.out.println("Truly pridects: " + correctPredicts);

        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
