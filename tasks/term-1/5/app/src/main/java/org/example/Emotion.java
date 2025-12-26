package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;
import com.opencsv.exceptions.CsvValidationException;

public class Emotion {

    private final StanfordCoreNLP pipeline;

    public Emotion() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("<br />", " ").trim();
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) {
            return "neutral";
        }
        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }
        Map<String, Integer> sentimentCounts = new HashMap<>();
        sentimentCounts.put("positive", 0);
        sentimentCounts.put("negative", 0);
        sentimentCounts.put("neutral", 0);
        for (CoreMap sentence : sentences) {
            String rawSentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String simplified = mapToSimple(rawSentiment);
            sentimentCounts.put(simplified, sentimentCounts.get(simplified) + 1);
        }
        return sentimentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
    }

    private String mapToSimple(String sentiment) {
        if (sentiment == null) return "neutral";
        String lower = sentiment.toLowerCase();
        switch (lower) {
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


    public List<ReviewRecord> loadDataset(String resourcePath) throws IOException {
        List<ReviewRecord> list = new ArrayList<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Ресурс не найден: " + resourcePath);
        }
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] nextLine;
            boolean firstLine = true;
            while (true) {
                try {
                    nextLine = reader.readNext();
                    if (nextLine == null) break;
                } catch (CsvValidationException e) {
                    System.err.println("Пропущена некорректная строка в CSV: " + e.getMessage());
                    continue;
                }
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (nextLine.length >= 2) {
                    String text = cleanText(nextLine[0]);
                    String label = nextLine[1].toLowerCase();
                    list.add(new ReviewRecord(text, label));
                }
            }
        }
        return list;
    }

    public static class ReviewRecord {
        public final String text;
        public final String label;

        public ReviewRecord(String text, String label) {
            this.text = text;
            this.label = label;
        }
    }
}