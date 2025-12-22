package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

public class SentimentAnalyzer {

    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    // Возвращает: positive / negative / neutral
    public String analyzeReview(String review) {
        if (review == null || review.isBlank()) {
            return "neutral";
        }

        Annotation doc = new Annotation(review);
        pipeline.annotate(doc);

        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        int sum = 0;
        for (CoreMap sentence : sentences) {
            String sent = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            switch (sent) {
                case "Very negative" -> sum += 0;
                case "Negative"      -> sum += 1;
                case "Neutral"       -> sum += 2;
                case "Positive"      -> sum += 3;
                case "Very positive" -> sum += 4;
            }
        }

        double avg = sum * 1.0 / sentences.size();

        if (avg < 1.5) return "negative";
        else if (avg > 2.5) return "positive";
        else return "neutral";
    }
}
