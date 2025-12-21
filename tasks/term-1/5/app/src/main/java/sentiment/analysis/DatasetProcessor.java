package sentiment.analysis;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class DatasetProcessor {
    public List<MovieReview> readReviewFromDataset(String filePath) {
        List<MovieReview> reviews = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // пропускаем первую строку
                    continue;
                }

                String[] columns = parseCSVLine(line);

                if (columns.length >= 2) {
                    String reviewText = columns[0].replaceAll("^\"|\"$", "").trim();
                    String actualSentiment = columns[1].replaceAll("^\"|\"$", "").trim().toLowerCase();

                    if (actualSentiment.equals("positive") || actualSentiment.equals("1")) {
                        actualSentiment = "positive";
                    } else if (actualSentiment.equals("negative") || actualSentiment.equals("0")) {
                        actualSentiment = "negative";
                    } else {
                        actualSentiment = "unknown";
                    }

                    reviewText = cleanText(reviewText);

                    if(!reviewText.trim().isEmpty()) {
                        MovieReview review = new MovieReview(reviewText.trim());
                        review.setActualSentiment(actualSentiment);
                        reviews.add(review);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading Dataset file: " + e.getMessage());
            e.printStackTrace();
        }

        return reviews;
    }

    private String[] parseCSVLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder currentColumn = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(currentColumn.toString());
                currentColumn = new StringBuilder();
            } else {
                currentColumn.append(c);
            }
        }
        columns.add(currentColumn.toString());

        return columns.toArray(new String[0]);
    }

    private String cleanText(String text) {
        if (text == null) return "";

        // удаление html тегов
        text = text.replace("<br />", " ")
                .replaceAll("<[^>]+>", "");

        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public void printResults(List<MovieReview> reviews) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("Analysis Results");
        System.out.println("\n" + "=".repeat(100));

        int correct = 0;
        int total = 0; // считаем только те, у которых известен фактический сентимент

        for(int i = 0; i < reviews.size(); i++) {
            MovieReview review = reviews.get(i);
            String actualSentiment = review.getActualSentiment();
            String predictedSentiment = review.getSentiment();

            // Проверяем на null перед использованием
            if (actualSentiment == null) {
                actualSentiment = "unknown";
            }
            if (predictedSentiment == null) {
                predictedSentiment = "unknown";
            }

            boolean isComparable = !"unknown".equals(actualSentiment);
            boolean isCorrect = false;

            if (isComparable) {
                isCorrect = actualSentiment.equals(predictedSentiment);
                if (isCorrect) correct++;
                total++;
            }

            System.out.printf("%d. Actual: %-10s | Predicted: %-10s | %s%n",
                    i + 1,
                    actualSentiment,
                    predictedSentiment,
                    !isComparable ? "N/A (unknown actual)" :
                            isCorrect ? "CORRECT" : "WRONG");
            System.out.printf("   Text: %s%n",
                    review.getReviewText().length() > 80 ?
                            review.getReviewText().substring(0, 80) + "..." :
                            review.getReviewText());
            System.out.println("-".repeat(100));
        }

        if (total > 0) {
            double accuracy = (correct * 100.0 / total);
            System.out.printf("\nAccuracy: %d/%d (%.2f%%)%n", correct, total, accuracy);
        } else {
            System.out.println("\nCannot calculate accuracy: no reviews with known actual sentiment.");
        }
    }
}