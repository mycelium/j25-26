package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;



public class App {
    private static StanfordCoreNLP pipeline;
    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }
    public static void main(String[] args) {
        String dataset = "src/main/res/Dataset.csv";
        try {
            String[][] data = loadReviewsAndSentiments(dataset);
            String[] reviews = data[0];
            String[] sentiments = data[1];

            System.out.println(reviews.length);
            System.out.println(sentiments.length);
            for (int i = 0; i < sentiments.length; i++){
                System.out.println("predict:" + analyzeSentiment(reviews[i]));
                System.out.println("real:" + analyzeSentiment(sentiments[i]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[][] loadReviewsAndSentiments(String csvFilePath) throws IOException {
        int lineCount = 0;
        try (BufferedReader counter = new BufferedReader(new FileReader(csvFilePath))) {
            while (counter.readLine() != null) {
                lineCount++;
            }
        }
        int dataSize = Math.max(0, lineCount - 1);

        String[] reviews = new String[dataSize];
        String[] sentiments = new String[dataSize];

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line = br.readLine();

            int index = 0;
            while ((line = br.readLine()) != null && index < dataSize) {
                int lastCommaIndex = line.lastIndexOf(',');
                if (lastCommaIndex == -1) {
                    continue;
                }

                String reviewPart = line.substring(0, lastCommaIndex);
                String sentimentPart = line.substring(lastCommaIndex + 1);

                String review = unescapeCsvField(reviewPart);
                String sentiment = unescapeCsvField(sentimentPart);

                reviews[index] = review;
                sentiments[index] = sentiment;
                index++;
            }
        }

        return new String[][]{reviews, sentiments};
    }

    private static String unescapeCsvField(String field) {
        if (field.startsWith("\"") && field.endsWith("\"")) {
            return field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field;
    }

    public static String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.isEmpty()) {
            return "neutral";
        }

        int totalScore = 0;
        for (CoreMap sentence : sentences) {
            String sentimentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            int score = sentimentClassToScore(sentimentClass);
            totalScore += score;
        }

        double average = (double) totalScore / sentences.size();
        int roundedAvg = (int) Math.round(average);
        return scoreToSentimentClass(roundedAvg);
    }

    private static int sentimentClassToScore(String cls) {
        switch (cls.toLowerCase()) {
            case "very negative": return 0;
            case "negative":      return 1;
            case "neutral":       return 2;
            case "positive":      return 3;
            case "very positive": return 4;
            default:              return 2; 
        }
    }

    private static String scoreToSentimentClass(int score) {
        switch (score) {
            case 0, 1: return "negative";
            case 2: return "neutral";
            case 3, 4: return "positive";
            default:
                if (score < 0) return "negative";
                else return "positive";
        }
    }
}