package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.util.*;

public class SentimentAnalyzer {
    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        System.out.println(" Loading Stanford CoreNLP models (tokenize, ssplit, parse, sentiment)...");
        long start = System.currentTimeMillis();

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        props.setProperty("tokenize.options", "untokenizable=noneKeep");

        this.pipeline = new StanfordCoreNLP(props);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("CoreNLP models loaded in " + elapsed + " ms");
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
            String detailed = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            return mapToSimpleSentiment(detailed);

        } catch (Exception e) {
            System.err.println(" Error analyzing: '" + text.substring(0, Math.min(50, text.length())) + "...' → fallback to 'neutral'");
            return "neutral";
        }
    }

    private static String mapToSimpleSentiment(String detailedSentiment) {
        if (detailedSentiment == null) return "neutral";

        String lower = detailedSentiment.toLowerCase().trim();
        return switch (lower) {
            case "very positive", "positive" -> "positive";
            case "very negative", "negative" -> "negative";
            case "neutral" -> "neutral";
            default -> "neutral";
        };
    }
}
