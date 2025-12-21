package sentiment.analysis;

import java.util.List;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String inputFile = "IMDB Dataset.csv";

        System.out.println("Starting sentiment analysis...");

        List<Review> reviews;
        ReviewProcessor processor = new ReviewProcessor();
        try {
            reviews = processor.loadReviewsFromCsv(inputFile);
            System.out.println("Loaded " + reviews.size() + " reviews from: " + inputFile);
        } catch (IOException e) {
            System.err.println("Failed to read the dataset file.");
            return;
        }

        ReviewAnalyzer analyzer;
        try {
            analyzer = new ReviewAnalyzer();
        } catch (Exception e) {
            System.err.println("Failed to initialize Stanford CoreNLP.");
            return;
        }
        System.out.println("Analyzing reviews...");

        int startReview = 0;
        int totalToShow = Math.min(10, reviews.size());
        int correct = 0;

        for (int i = startReview; i < totalToShow; i++) {
            Review review = reviews.get(i);
            analyzer.analyzeReview(review);

            System.out.println("Review " + (i + 1) + ":");
            System.out.println("Actual sentiment: " + review.getActualSentiment());
            System.out.println("Calculated sentiment: " + review.getCalculatedSentiment());
            System.out.println("---");

            boolean isCorrect = review.getActualSentiment().equalsIgnoreCase(review.getCalculatedSentiment());
            if (isCorrect) {
                correct++;
            }
        }

        System.out.println("=== Result ===");
        System.out.println("Correct: " + correct + "/" + (totalToShow - startReview));
    }
}
