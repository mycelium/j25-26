package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

public class App {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        App.class.getClassLoader().getResourceAsStream("Dataset.csv")))) {

            String[] nextLine;
            boolean isFirstLine = true;
            int correct = 0;
            int total = 0;

            try {
                while ((nextLine = reader.readNext()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String review = nextLine[0].trim();
                    String trueSentiment = nextLine[1].trim();

                    if (!review.isEmpty()) {
                        String predictedSentiment = getSentiment(pipeline, review);

                        System.out.println("Review: " + review);
                        System.out.println("Predicted: " + predictedSentiment);
                        System.out.println("Actual: " + trueSentiment);
                        System.out.println("Match: " + (predictedSentiment.equals(trueSentiment) ? "YES" : "NO"));
                        System.out.println("---");

                        if (predictedSentiment.equals(trueSentiment)) {
                            correct++;
                        }
                        total++;
                    }
                }
            } catch (CsvValidationException e) {
                System.err.println("Ошибка при чтении CSV: " + e.getMessage());
            }

            if (total > 0) {
                System.out.println("Accuracy: " + (double) correct / total * 100 + "%");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

    }

    private static String getSentiment(StanfordCoreNLP pipeline, String text) {
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        List<Integer> sentimentScores = new ArrayList<>();

        for (CoreMap sentence : sentences) {
            String sentimentLabel = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            if (sentimentLabel != null) {
                int score = mapSentimentLabelToScore(sentimentLabel);
                sentimentScores.add(score);
            } else {
                sentimentScores.add(2); // default neutral
            }
        }
        double averageScore = sentimentScores.stream().mapToInt(Integer::intValue).average().orElse(2.0);

        return mapAverageScoreToString(averageScore);
    }

    private static int mapSentimentLabelToScore(String label) {
        switch (label.toLowerCase()) {
            case "very negative":
                return 0;
            case "negative":
                return 1;
            case "neutral":
                return 2;
            case "positive":
                return 3;
            case "very positive":
                return 4;
            default:
                return 2;
        }
    }

    private static String mapAverageScoreToString(double averageScore) {
        if (averageScore < 1.5) {
            return "negative";
        } else if (averageScore > 2.5) {
            return "positive";
        } else {
            return "neutral";
        }
    }
}

// В случае отсутствия дополнительного ранжирования (только 3 оценки, вместо 5),
// было слишком много ощибок и неточностей