package org.example;

import java.io.IOException;
import java.util.List;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    private static final int REVIEWS_COUNT = 13;
    private static final String DATASET_NAME = "IMDB Dataset.csv";

    public static void main(String[] args) {
        try {
            List<ReviewLoader.MovieReview> dataset = ReviewLoader.fetchAllReviews(DATASET_NAME);
            Process analyzer = new Process();

            int accurate = 0;

            System.out.println("Program start\n");

            for (int i = 0; i < REVIEWS_COUNT; i++) {
                ReviewLoader.MovieReview item = dataset.get(i);
                String prediction = analyzer.inferMood(item.content());

                System.out.printf(
                        """
                                Review #%d
                                Text: %s
                                Actual: %s
                                Predict: %s
                                
                                """,
                    i + 1,
                    item.content(),
                    item.mood(),
                    prediction
                );

                if (item.mood().equalsIgnoreCase(prediction)) {
                    accurate++;
                }
            }

            double accuracy = (double) accurate / REVIEWS_COUNT * 100;
            System.out.printf("Total accuracy on %d samples: %.2f%%\n", REVIEWS_COUNT, accuracy);

        } catch (IOException e) {
            System.err.println("Failed to load dataset: " + e.getMessage());
            System.exit(1);
        }
    }
}