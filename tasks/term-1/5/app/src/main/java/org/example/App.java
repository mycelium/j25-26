package org.example;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        StanfordCoreNLP pipeline = createPipeline();

        try (InputStream is = App.class.getClassLoader().getResourceAsStream("reviews.txt")) {
            if (is == null) {
                System.err.println("Файл reviews.txt не найден в resources");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String review = line.trim();
                    if (review.isEmpty()) {
                        continue;
                    }

                    String label = analyzeSentiment(pipeline, review);
                    System.out.printf("%-8s %s%n", label, review);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static StanfordCoreNLP createPipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        return new StanfordCoreNLP(props);
    }

    static String analyzeSentiment(StanfordCoreNLP pipeline, String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);
        List<CoreSentence> sentences = doc.sentences();

        if (sentences.isEmpty()) {
            return "neutral";
        }

        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (CoreSentence sentence : sentences) {
            String coreNlpClass = sentence.sentiment();
            String normalized = coreNlpClass.toUpperCase().replace(" ", "_");

            switch (normalized) {
                case "VERY_NEGATIVE":
                case "NEGATIVE":
                    negativeCount++;
                    break;
                case "VERY_POSITIVE":
                case "POSITIVE":
                    positiveCount++;
                    break;
                case "NEUTRAL":
                default:
                    neutralCount++;
                    break;
            }
        }

        if (positiveCount > negativeCount && positiveCount > neutralCount) {
            return "positive";
        } else if (negativeCount > positiveCount && negativeCount > neutralCount) {
            return "negative";
        } else {
            return "neutral";
        }
    }
}
