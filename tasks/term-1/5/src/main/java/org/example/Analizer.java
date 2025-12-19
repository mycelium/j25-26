package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;


public class Analizer {
    private StanfordCoreNLP pipeline;

    public Analizer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String review) {
        if (review == null || review.trim().isEmpty()) {
            return "neutral";
        }
        Annotation annotation = pipeline.process(review);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> sentimentCount = new HashMap<>();
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String finSent = finalSentiment(sentiment);
            sentimentCount.put(finSent, sentimentCount.getOrDefault(finSent, 0) + 1);
        }

        return sentimentCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    private String finalSentiment(String coreNLPSentiment) {
        if (coreNLPSentiment == null) return "neutral";

        switch (coreNLPSentiment.toLowerCase()) {
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
}
