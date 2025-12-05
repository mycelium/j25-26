package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class SentimentAnalyzer {

    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        System.out.println("Loading NLP models...");

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("coref.algorithm", "neural");

        this.pipeline = new StanfordCoreNLP(props);
        System.out.println("Models loaded successfully!");
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        try {
            CoreDocument document = new CoreDocument(text);
            pipeline.annotate(document);

            List<String> sentiments = new ArrayList<>();
            for (CoreSentence sentence : document.sentences()) {
                String sentiment = sentence.sentiment();
                sentiments.add(sentiment);
            }

            return getOverallSentiment(sentiments);

        } catch (Exception e) {
            System.err.println("Error analyzing text: " + e.getMessage());
            return "neutral";
        }
    }

    private String getOverallSentiment(List<String> sentiments) {
        if (sentiments.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> sentimentCount = new HashMap<>();
        for (String sentiment : sentiments) {
            sentimentCount.put(sentiment, sentimentCount.getOrDefault(sentiment, 0) + 1);
        }

        String dominantSentiment = "Neutral";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : sentimentCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantSentiment = entry.getKey();
            }
        }

        return convertToSimpleSentiment(dominantSentiment);
    }

    private String convertToSimpleSentiment(String stanfordSentiment) {
        switch (stanfordSentiment.toLowerCase()) {
            case "very positive":
            case "positive":
                return "positive";
            case "very negative":
            case "negative":
                return "negative";
            default:
                return "neutral";
        }
    }

    public void analyzeFile(String inputFilePath) throws IOException {
        System.out.println("\nReading file: " + inputFilePath);

        List<String> lines = Files.readAllLines(Paths.get(inputFilePath));
        List<String> results = new ArrayList<>();

        System.out.println("Found reviews: " + lines.size());
        System.out.println("\n---------------------");

        int reviewNumber = 1;
        for (String line : lines) {
            String review = line.trim();
            if (!review.isEmpty()) {
                String sentiment = analyzeSentiment(review);

                String result = String.format("Review %d: %s", reviewNumber, sentiment);
                String details = String.format("   Text: %s",
                        review.length() > 60 ? review.substring(0, 60) + "..." : review);

                System.out.println(result);
                System.out.println(details);
                System.out.println();

                results.add(sentiment);
                reviewNumber++;
            }
        }

        printStatistics(results);
    }

    private void printStatistics(List<String> sentiments) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("positive", 0);
        stats.put("negative", 0);
        stats.put("neutral", 0);

        for (String sentiment : sentiments) {
            stats.put(sentiment, stats.get(sentiment) + 1);
        }

        int total = sentiments.size();

        System.out.println("--------------------------");
        System.out.println("Total reviews: " + total);
        System.out.println("Positive: " + stats.get("positive") + " (" +
                (total > 0 ? (stats.get("positive") * 100 / total) : 0) + "%)");
        System.out.println("Negative: " + stats.get("negative") + " (" +
                (total > 0 ? (stats.get("negative") * 100 / total) : 0) + "%)");
        System.out.println("Neutral: " + stats.get("neutral") + " (" +
                (total > 0 ? (stats.get("neutral") * 100 / total) : 0) + "%)");
    }

    public static void main(String[] args) {
        System.out.println("MOVIE REVIEW SENTIMENT ANALYZER");

        SentimentAnalyzer analyzer = new SentimentAnalyzer();

        try {
            if (args.length > 0) {
                String filePath = args[0];
                analyzer.analyzeFile(filePath);
            } else {
                System.out.println("No file specified. Using demo mode...");

                String[] demoReviews = {
                        "This movie is absolutely fantastic! Great acting and storyline.",
                        "I hated this film. It was boring and poorly made.",
                        "The movie was okay. Nothing special but not terrible either.",
                        "What a waste of time! The worst movie I've ever seen.",
                        "Brilliant cinematography and amazing performances by the cast."
                };

                System.out.println("\n-------------");
                for (int i = 0; i < demoReviews.length; i++) {
                    String sentiment = analyzer.analyzeSentiment(demoReviews[i]);
                    System.out.printf("Review %d: %s%n", i + 1, sentiment);
                    System.out.printf("Text: %s%n%n",
                            demoReviews[i].length() > 50 ?
                                    demoReviews[i].substring(0, 50) + "..." : demoReviews[i]);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}