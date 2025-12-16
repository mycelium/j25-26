package sentiment.analysis;

import java.util.List;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String inputFile = "D:/university/5_semester/java/j25-26/tasks/term-1/5/app/src/main/resources/IMDB Dataset.csv";

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

        int total = Math.min(30, reviews.size());
        List<Review> sample = reviews.subList(0, total);
        for (Review review : sample) {
            analyzer.analyzeReview(review);
        }

        processor.printStatistics(sample);

        System.out.println("\n=== Sample Reviews ===");
        for (int i = 0; i < Math.min(5, reviews.size()); i++) {
            Review r = reviews.get(i);
            String text = r.getText();
            if (text.length() > 80) {
                text = text.substring(0, 77) + "...";
            }
            System.out.printf("Review #%d [%s]: %s%n",
                    r.getId(), r.getSentiment(), text);
        }
    }
}
