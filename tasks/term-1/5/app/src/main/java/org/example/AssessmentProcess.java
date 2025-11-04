package org.example;

import java.util.*;
import java.io.*;

public class AssessmentProcess
{
    private EmotionalContext emotionalAnaly;

    public AssessmentProcess()
    {
        this.emotionalAnaly = new EmotionalContext();
    }

    public List<OneReview> analyzeFullTextFile(String filepathInput, String filepathOutput) {
        List<OneReview> reviews = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filepathInput));
             FileWriter writer = new FileWriter(filepathOutput)) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String reviewText = line.trim();

                if (!reviewText.isEmpty()) {

                    String sentiment = emotionalAnaly.BillingEmotional(reviewText);

                    OneReview review = new OneReview("Re" + count, reviewText);
                    review.setTextEmotions(sentiment);
                    reviews.add(review);

                    writer.write(reviewText + " :: " + sentiment + "\n");

                    count++;
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        return reviews;
    }
}