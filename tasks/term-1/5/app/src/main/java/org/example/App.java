package org.example;

import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        try {
            final String filePath = "IMDB Dataset.csv";

            Processor textProcessor = new Processor();
            List<Processor.Entry> dataSet = textProcessor.parseData(filePath);

            if (dataSet.isEmpty()) {
                System.err.println("Датасет пуст или не найден.");
                return;
            }

            Emotion classifier = new Emotion();

            System.out.println("--- Sentiment Analysis with CoreNLP ---\n");

            int matches = 0;
            int sampleSize = 10;
            Random rng = new Random();

            for (int i = 0; i < sampleSize; i++) {
                int randomIndex = rng.nextInt(dataSet.size());
                Processor.Entry entry = dataSet.get(randomIndex);

                String prediction = classifier.classify(entry.getContent());
                String real = entry.getLabel().toLowerCase();

                String displayText = entry.getContent().length() > 150
                        ? entry.getContent().substring(0, 150) + "..."
                        : entry.getContent();

                System.out.println("отзыв №" + (i + 1));
                System.out.println(displayText);
                System.out.println("оценка: " + prediction);
                System.out.println("------------------------");

                if (real.equals(prediction)) {
                    matches++;
                }
            }

//            double accuracy = (double) matches / sampleSize * 100;
//            System.out.printf("\n точность на %d случайных примерах: %.2f%%\n", sampleSize, accuracy);

        } catch (Exception ex) {
            System.err.println(" error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}