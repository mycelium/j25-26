package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Analyzer {

    private StanfordCoreNLP nlpPipeline;

    public Analyzer() {
        System.out.println("Initializing NLP pipeline...");

        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        properties.setProperty("coref.algorithm", "neural");

        this.nlpPipeline = new StanfordCoreNLP(properties);
        System.out.println("NLP pipeline initialized!");
    }

    public String getSentiment(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return "neutral";
        }

        try {
            CoreDocument document = new CoreDocument(inputText);
            nlpPipeline.annotate(document);

            List<String> sentenceSentiments = new ArrayList<>();
            for (CoreSentence sentence : document.sentences()) {
                sentenceSentiments.add(sentence.sentiment());
            }

            return determineDominantSentiment(sentenceSentiments);

        } catch (Exception ex) {
            System.err.println("Error during sentiment analysis: " + ex.getMessage());
            return "neutral";
        }
    }

    private String determineDominantSentiment(List<String> sentenceSentiments) {
        if (sentenceSentiments.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> sentimentFrequency = new HashMap<>();
        for (String sentiment : sentenceSentiments) {
            sentimentFrequency.put(sentiment, sentimentFrequency.getOrDefault(sentiment, 0) + 1);
        }

        String mainSentiment = "Neutral";
        int maxFrequency = 0;
        for (Map.Entry<String, Integer> entry : sentimentFrequency.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                mainSentiment = entry.getKey();
            }
        }

        return convertToStandardSentiment(mainSentiment);
    }

    private String convertToStandardSentiment(String detailedSentiment) {
        switch (detailedSentiment.toLowerCase()) {
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

    public void processReviewFile(String filePath) throws IOException {
        System.out.println("\nProcessing file: " + filePath);

        List<String> reviewLines = Files.readAllLines(Paths.get(filePath));
        List<String> sentimentResults = new ArrayList<>();

        System.out.println("Reviews found: " + reviewLines.size());
        System.out.println("\n---------------------");

        int reviewIndex = 1;
        for (String review : reviewLines) {
            String cleanedReview = review.trim();
            if (!cleanedReview.isEmpty()) {
                String sentiment = getSentiment(cleanedReview);

                String output = String.format("Review %d: %s", reviewIndex, sentiment);
                String snippet = String.format("   Text: %s",
                        cleanedReview.length() > 60 ? cleanedReview.substring(0, 60) + "..." : cleanedReview);

                System.out.println(output);
                System.out.println(snippet);
                System.out.println();

                sentimentResults.add(sentiment);
                reviewIndex++;
            }
        }

        displaySentimentStatistics(sentimentResults);
    }

    private void displaySentimentStatistics(List<String> sentiments) {
        Map<String, Integer> sentimentCount = new HashMap<>();
        sentimentCount.put("positive", 0);
        sentimentCount.put("negative", 0);
        sentimentCount.put("neutral", 0);

        for (String sentiment : sentiments) {
            sentimentCount.put(sentiment, sentimentCount.get(sentiment) + 1);
        }

        int totalReviews = sentiments.size();

        System.out.println("--------------------------");
        System.out.println("Total reviews: " + totalReviews);
        System.out.println("Positive: " + sentimentCount.get("positive") + " (" +
                (totalReviews > 0 ? (sentimentCount.get("positive") * 100 / totalReviews) : 0) + "%)");
        System.out.println("Negative: " + sentimentCount.get("negative") + " (" +
                (totalReviews > 0 ? (sentimentCount.get("negative") * 100 / totalReviews) : 0) + "%)");
        System.out.println("Neutral: " + sentimentCount.get("neutral") + " (" +
                (totalReviews > 0 ? (sentimentCount.get("neutral") * 100 / totalReviews) : 0) + "%)");
    }

    public static void main(String[] args) {
        System.out.println("MOVIE REVIEW SENTIMENT ANALYZER");

        Analyzer analyzer = new Analyzer();

        try {
            if (args.length > 0) {
                String inputFile = args[0];
                analyzer.processReviewFile(inputFile);
            } else {
                System.out.println("No file provided. Running demo mode...");

                String[] sampleReviews = {
                        "This movie was great!",
                        "I didn’t like it at all.",
                        "It was okay, nothing special.",
                        "Really enjoyed the movie!",
                        "It was boring and too long.",
                        "I loved it! Amazing movie.",
                        "It wasn’t bad, but not great either.",
                        "Waste of time, didn’t enjoy it.",
                        "Good movie, would watch again.",
                        "It was fine, but I expected more."
                };

                System.out.println("\n-------------");
                for (int i = 0; i < sampleReviews.length; i++) {
                    String sentiment = analyzer.getSentiment(sampleReviews[i]);
                    System.out.printf("Review %d: %s%n", i + 1, sentiment);
                    System.out.printf("Text: %s%n%n",
                            sampleReviews[i].length() > 50 ?
                                    sampleReviews[i].substring(0, 50) + "..." : sampleReviews[i]);
                }
            }

        } catch (Exception ex) {
            System.err.println("An error occurred: " + ex.getMessage());
        }
    }
}
