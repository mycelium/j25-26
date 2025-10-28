package org.example;

import java.util.List;

public class App {
    public static void main(String[] args) {

        ReviewProcessor processor = new ReviewProcessor(); //создаем процессор для анализа отзывов
        List<MovieReview> reviews = processor.processReviews("C:\\Users\\Sofia\\IdeaProjects\\j25-26\\tasks\\term-1\\5\\reviews.txt"); //анализ отзывов из файла
        saveResults(reviews); //результаты в файл statiment_results.txt
    }

    private static void saveResults(List<MovieReview> reviews) {
        try (var writer = new java.io.PrintWriter("sentiment_results.txt")) {
            writer.println("id\tSentiment\tReview");
            for (MovieReview review : reviews) {
                writer.printf("%s\t%s\t%s%n",
                        review.getId(), review.getSentiment(), review.getText());
            }
        } catch (Exception e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }
}