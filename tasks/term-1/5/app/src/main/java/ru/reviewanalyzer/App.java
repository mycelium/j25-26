package ru.reviewanalyzer;

import java.util.List;


public class App {
    public static void main(String[] args) {
        DatasetFormatter formatter = new DatasetFormatter();

        List<Review> reviews = formatter.datasetToReviews(
                "src/main/resources/IMDB Dataset.csv"
        );

        ReviewAnalyzer analyzer = new ReviewAnalyzer();

        System.out.println("---- Reviews analysis ----");

        int correct = 0;
        int total = 10;
        for (int i = 0; i < total; i++) {
            int randomID = (int) (Math.random() * (reviews.size()-1));

            Review review = reviews.get(randomID);
            String predictedSentiment = analyzer.analyzeReview(review.text());

            System.out.println("Review " + (i + 1) + ":");
            System.out.println("Text: " + (
                    review.text().length() > 100 ?
                            review.text().substring(0, 100) + "..." :
                            review.text()
            ));
            System.out.println("Actual: " + review.realSentiment() + " | Predicted: " + predictedSentiment);
            System.out.println("========================================");

            if (review.realSentiment().equals(predictedSentiment)) {
                correct++;
            }
        }

        System.out.println(
            "Accuracy on sample: "
                + String.format("%.2f", (correct * 100.0 / total)) + "%"
        );
    }
}