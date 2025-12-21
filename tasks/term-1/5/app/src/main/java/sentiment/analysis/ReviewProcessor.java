package sentiment.analysis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReviewProcessor {

    public List<Review> loadReviewsFromCsv(String filePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Ресурс не найден в classpath: " + filePath);
            }

            List<Review> reviews = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                int index = 1;

                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String cleanLine = line;
                    if (cleanLine.startsWith("\"") && cleanLine.endsWith("\"")) {
                        cleanLine = cleanLine.substring(1, cleanLine.length() - 1);
                    }

                    int lastComma = cleanLine.lastIndexOf(',');
                    if (lastComma == -1) {
                        System.err.println("Skipping invalid line: " + line);
                        continue;
                    }

                    String text = cleanLine.substring(0, lastComma).trim();
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        text = text.substring(1, text.length() - 1);
                    }

                    reviews.add(new Review(index++, text));
                }
            }
            return reviews;
        }
    }

    public void printStatistics(List<Review> reviews) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("positive", 0);
        stats.put("negative", 0);
        stats.put("neutral", 0);

        for (Review review : reviews) {
            String sentiment = review.getSentiment();
            stats.put(sentiment, stats.getOrDefault(sentiment, 0) + 1);
        }

        int total = reviews.size();
        System.out.println("\n=== Sentiment Analysis Results ===");
        System.out.println("Total reviews: " + total);
        System.out.println("Positive: " + stats.get("positive") +
                " (" + String.format("%.1f", 100.0 * stats.get("positive") / total) + "%)");
        System.out.println("Negative: " + stats.get("negative") +
                " (" + String.format("%.1f", 100.0 * stats.get("negative") / total) + "%)");
        System.out.println("Neutral: " + stats.get("neutral") +
                " (" + String.format("%.1f", 100.0 * stats.get("neutral") / total) + "%)");
    }
}
