package org.example;

import java.util.List;
import java.util.Random;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }
    public static void main(String[] args) {
        try {
            final String filePath = "../IMDB Dataset.csv";
            TextProcessor textProcessor = new TextProcessor();
            List<TextProcessor.Entry> dataSet = textProcessor.parseData(filePath);

            EmotionClassifier classifier = new EmotionClassifier();

            System.out.println("Emotion Classification:");
            
            int matches = 0;
            int sampleSize = 30;
            
            Random rng = new Random();
            for (int i = 0; i < sampleSize; i++) {
                int randomIndex = rng.nextInt(dataSet.size());
                TextProcessor.Entry entry = dataSet.get(randomIndex);
                
                String classificationResult = classifier.classify(entry.getContent());
                
                String displayText = entry.getContent().length() > 200 
                    ? entry.getContent().substring(0, 200) + "..." 
                    : entry.getContent();
                
                System.out.println("Sample №" + (i + 1));
                System.out.println("Review: " + displayText);
                System.out.println("Real sentiment: " + entry.getLabel() + " -> Prediction: " + classificationResult);
                System.out.println("----------");

                if (entry.getLabel().equalsIgnoreCase(classificationResult)) {
                    matches++;
                }
            }
            
            double accuracy = (double) matches / sampleSize * 100;
            System.out.printf("Accuracy over %d samples: %.2f%%%n", sampleSize, accuracy);

        } catch (Exception ex) {
            System.err.println("Error while processing dataset: " + ex.getMessage());
        }
    }
}