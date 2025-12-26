package org.example;

import java.util.*;

public class App {

    public static void main(String[] args) {
        try {
            Emotion analyzer = new Emotion();
            String datasetPath = "IMDB Dataset.csv";
            List<Emotion.ReviewRecord> dataset = analyzer.loadDataset(datasetPath);
            System.out.print("---анализатор отзывов на фильмы---\n");
            System.out.println("загружено отзывов: " + dataset.size());
            System.out.println("10 случайных отзывов...\n");
            Collections.shuffle(dataset);
            List<Emotion.ReviewRecord> sample = dataset.subList(0, 20);
            int correctPredictions = 0;
            final int SAMPLE = 10;
            for (int i = 0; i < SAMPLE; i++) {
                Emotion.ReviewRecord review = sample.get(i);
                String predictedLabel = analyzer.analyzeSentiment(review.text);
                if (predictedLabel.equalsIgnoreCase(review.label)) {
                    correctPredictions++;
                }
                String displayText = (review.text.length() > 120)
                        ? review.text.substring(0, 120) + "..."
                        : review.text;

                System.out.printf(
                        "отзыв %d: \"%s\", \nпредсказано: %s, \nфакт: %s%n",
                        i + 1, displayText, predictedLabel, review.label
                );
            }

            System.out.println();
            System.out.printf(
                    "точность: %d из %d (%.0f%%)%n",
                    correctPredictions, SAMPLE, (correctPredictions * 100.0 / SAMPLE)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}