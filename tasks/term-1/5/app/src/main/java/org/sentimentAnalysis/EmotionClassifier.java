package org.sentimentAnalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class EmotionClassifier {

    private final StanfordCoreNLP nlp;

    public EmotionClassifier() {
        Properties config = new Properties();
        config.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        this.nlp = new StanfordCoreNLP(config);
    }

    public String classify(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "neutral";
        }

        Annotation doc = nlp.process(content);
        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> c = new HashMap<>();
        for (CoreMap sentence : sentences) {
            String rawEmotion = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String normalized = normalize(rawEmotion);
            c.merge(normalized, 1, Integer::sum);
        }

        return c.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
    }

    private String normalize(String raw) {
        if (raw == null) return "neutral";
        String lc = raw.toLowerCase();
        return switch (lc) {
            case "very positive", "positive" -> "positive";
            case "very negative", "negative" -> "negative";
            default -> "neutral";
        };
    }
}