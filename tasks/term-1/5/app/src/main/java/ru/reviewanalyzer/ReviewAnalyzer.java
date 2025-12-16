package ru.reviewanalyzer;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.util.*;

public class ReviewAnalyzer {
    private StanfordCoreNLP pipeline;

    public ReviewAnalyzer() {
        Properties props = new Properties();
        props.setProperty("tokenize.language", "en");
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeReview(String text) {
        if (text == null || text.trim().isEmpty()) return "neutral";

        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) return "neutral";

        int neutral = 0, positive = 0, negative = 0;
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            if (sentiment == null) neutral++;
            else {
                switch (sentiment.toLowerCase()) {
                    case "very positive":
                    case "positive": positive++; break;
                    case "very negative":
                    case "negative": negative++; break;
                    default: neutral++;
                }
            }
        }

        return positive > negative
            ? (positive > neutral ? "positive" : "neutral")
            : (negative > neutral ? "negative" : "neutral");
    }
}