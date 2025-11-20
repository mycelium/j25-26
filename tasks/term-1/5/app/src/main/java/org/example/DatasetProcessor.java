package org.example;

import org.example.MovieReview;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class DatasetProcessor {
    public List<MovieReview> readReviewFromDataset (String filePath) {
        List<MovieReview> reviews = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // пропускаем первую строку
                    continue;
                }

                String reviewText = parseReviewText(line);
                if(reviewText != null && !reviewText.trim().isEmpty()) {
                    reviews.add(new MovieReview(reviewText.trim()));
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading Dataset file: " + e.getMessage());
        }

        return reviews;
    }

    private String parseReviewText(String line) {
        String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (columns.length >= 1) {
            String text = columns[0].replaceAll("^\"|\"$", "").trim();
            text = cleanText(text);
            return text;
        }

        return null;
    }


    private String cleanText(String text) {
        if (text == null) return "";

        // удаление всяких html тегов
        text = text.replace("<br />", " ")
                .replaceAll("<[^>]+>", "");

        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public void printResults(List<MovieReview> reviews) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("Analysis results");
        System.out.println("\n" + "=".repeat(100));

        for(int i = 0; i < reviews.size(); i++) {
            MovieReview review = reviews.get(i);
            String sentiment = review.getSentiment();

            System.out.printf("%d. %s%n", i + 1, review.toString());
            System.out.println("-".repeat(100));
        }
    }
}
