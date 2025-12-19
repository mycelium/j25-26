package org.example;

import java.nio.file.Path;
import java.util.List;

public class App {
    private static final String DEFAULT_INPUT = "reviews.cvs";
    private static final String OUTPUT_FILE = "sentiment_results.txt";

    public static void main(String[] args) {
        String inputPath = args.length > 0 ? args[0] : DEFAULT_INPUT;
        long startTime = System.currentTimeMillis();
        try {
            ReviewProcessor processor = new ReviewProcessor();

            long procStart = System.currentTimeMillis();
            List<MovieReview> reviews = processor.processReviews(inputPath);
            long procTime = System.currentTimeMillis() - procStart;

            saveResults(reviews);
            long totalTime = System.currentTimeMillis() - startTime;

        } catch (Exception e) {
            System.err.println(" Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void saveResults(List<MovieReview> reviews) {
        try (var writer = new java.io.PrintWriter(
                java.nio.file.Files.newBufferedWriter(
                        java.nio.file.Paths.get(OUTPUT_FILE),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            writer.println("id\tSentiment\tReview");
            for (MovieReview review : reviews) {
                String safeText = review.getText()
                        .replace("\t", "    ")
                        .replace("\n", " ")
                        .replace("\r", " ");
                writer.printf("%s\t%s\t%s%n",
                        review.getId(),
                        review.getSentiment(),
                        safeText);
            }
            System.out.println(" Results saved to: " + OUTPUT_FILE);

        } catch (Exception e) {
            System.err.println(" Error saving results: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
