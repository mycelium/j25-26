package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
import java.util.stream.Collectors;

public class Emotion {

    private final StanfordCoreNLP pipeline;

    public Emotion() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String classify(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Long> counts = sentences.stream()
                .map(sentence -> sentence.get(SentimentCoreAnnotations.SentimentClass.class))
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
    }
    private String normalize(String raw) {
        String lc = raw.toLowerCase();
        if (lc.contains("positive")) return "positive";
        if (lc.contains("negative")) return "negative";
        return "neutral";
    }
}