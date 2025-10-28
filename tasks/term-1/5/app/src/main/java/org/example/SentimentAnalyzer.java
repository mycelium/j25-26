package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.util.*;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        try {
            Annotation document = new Annotation(text);
            pipeline.annotate(document);

            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences == null || sentences.isEmpty()) {
                return "neutral";
            }

            CoreMap sentence = sentences.get(0);
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            return mapToSimpleSentiment(sentiment);

        } catch (Exception e) {
            System.err.println("Error analyzing: '" + text + "' - " + e.getMessage());
            return "neutral";
        }
    }

    private String mapToSimpleSentiment(String detailedSentiment) {
        if (detailedSentiment == null) return "neutral";

        switch (detailedSentiment.toLowerCase()) {
            case "very positive":
            case "positive":
                return "positive";
            case "very negative":
            case "negative":
                return "negative";
            case "neutral":
                return "neutral";
            default:
                return "neutral";
        }
    }
}