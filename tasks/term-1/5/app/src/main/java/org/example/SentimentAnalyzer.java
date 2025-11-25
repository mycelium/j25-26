package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;
    public SentimentAnalyzer() {
        Properties config = new Properties();
        config.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(config);
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