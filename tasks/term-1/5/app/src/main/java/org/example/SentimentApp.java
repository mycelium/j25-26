package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SentimentApp {

    public static void main(String[] args) throws IOException {
        SentimentAnalyzer analyzer = new SentimentAnalyzer();

        try (InputStream is = SentimentApp.class.getResourceAsStream("/data/IMDB Dataset.csv"); ) {
            if (is == null) {
                System.err.println("IMDB Dataset.csv resource not found");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                //String header = br.readLine(); // пропускаем заголовок
                String line;
                int idx = 1;
                while ((line = br.readLine()) != null && idx <= 50) {
                    int firstComma = line.indexOf(',');
                    if (firstComma < 0) continue;

                    String review = line.substring(firstComma + 1).trim();
                    if (review.startsWith("\"") && review.endsWith("\"") && review.length() >= 2) {
                        review = review.substring(1, review.length() - 1);
                    }

                    String label = analyzer.analyzeReview(review);

                    String shortText = review.length() > 120
                            ? review.substring(0, 120) + "..."
                            : review;

                    System.out.printf("%3d: %-8s | %s%n", idx, label, shortText);

                    idx++;   
                }

            }
        }
    }
}
