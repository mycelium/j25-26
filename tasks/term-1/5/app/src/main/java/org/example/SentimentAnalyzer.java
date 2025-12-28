package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

/**
 * Анализатор тональности текста с использованием Stanford CoreNLP.
 */
public class SentimentAnalyzer {

    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public SentimentAnalyzer(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Анализирует тональность текста.
     * @param text текст для анализа
     * @return "positive", "negative" или "neutral"
     */
    public String analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        String mainSentiment = "neutral";
        int longestSentenceLength = 0;

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String sentenceText = sentence.toString();
            if (sentenceText.length() > longestSentenceLength) {
                longestSentenceLength = sentenceText.length();
                mainSentiment = sentiment;
            }
        }

        return simplifySentiment(mainSentiment);
    }

    /**
     * Возвращает детальную тональность (Very positive, Positive, Neutral, Negative, Very negative).
     * @param text текст для анализа
     * @return детальная тональность
     */
    public String analyzeDetailed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        String mainSentiment = "Neutral";
        int longestSentenceLength = 0;

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) {
            return "Neutral";
        }

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String sentenceText = sentence.toString();
            if (sentenceText.length() > longestSentenceLength) {
                longestSentenceLength = sentenceText.length();
                mainSentiment = sentiment;
            }
        }

        return mainSentiment;
    }

    /**
     * Анализирует список текстов.
     * @param texts список текстов
     * @return карта текст -> тональность
     */
    public Map<String, String> analyzeBatch(List<String> texts) {
        if (texts == null) {
            return new HashMap<>();
        }

        Map<String, String> results = new LinkedHashMap<>();
        for (String text : texts) {
            results.put(text, analyze(text));
        }
        return results;
    }

    /**
     * Упрощает детальную тональность до трех категорий.
     */
    public String simplifySentiment(String detailed) {
        if (detailed == null) {
            return "neutral";
        }
        return switch (detailed) {
            case "Very positive", "Positive" -> "positive";
            case "Very negative", "Negative" -> "negative";
            default -> "neutral";
        };
    }
}

