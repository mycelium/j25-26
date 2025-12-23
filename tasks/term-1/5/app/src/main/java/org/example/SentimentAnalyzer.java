package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        
        if (sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> sentimentCount = new HashMap<>();
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String simpleSentiment = mapToSimpleSentiment(sentiment);
            sentimentCount.put(simpleSentiment, sentimentCount.getOrDefault(simpleSentiment, 0) + 1);
        }
        
        return sentimentCount.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        
    }
    private String mapToSimpleSentiment(String coreNLPSentiment) {
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
