package org.example;

import org.example.data.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class App {

    private static final int MAX_REVIEWS_TO_DISPLAY = 15;

    public static void main(String[] args) {
        String filePath = "IMDB Dataset.csv";

        SentimentProcessor processor = new SentimentProcessor();

        try {
            List<Review> reviews = CSVReviewFileProcessor.readReviews(filePath);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int maxReviewID = reviews.size();
            int correctPredictions = 0;
            for (int i = 0; i < MAX_REVIEWS_TO_DISPLAY; ++i) {
                int reviewID = random.nextInt(0, maxReviewID);
                Review review = reviews.get(reviewID);

                String sentiment = processor.processReview(review);
                System.out.printf(
                        """
                        ID: %d | #%d
                            Review text: %s
                                Predicted sentiment: %s
                                Actual sentiment: %s
                        
                        """,
                        i,
                        reviewID,
                        review.getText(),
                        sentiment,
                        review.getSentiment()
                );

                if (sentiment.equalsIgnoreCase(review.getSentiment())) {
                    correctPredictions += 1;
                }
            }

            System.out.printf(
                """
                Total accuracy on given CSV file:
                   %.2f %%
                """,
                (float)correctPredictions / MAX_REVIEWS_TO_DISPLAY * 100
            );
        }
        catch (IOException e) {
            System.err.println(
                    "[ERROR] Couldn't process CSV file.\n" + e.getMessage()
            );
        }
    }

}