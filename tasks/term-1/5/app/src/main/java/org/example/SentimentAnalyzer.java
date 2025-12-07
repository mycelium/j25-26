package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SentimentAnalyzer {

    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("<br />", " ").trim();
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) return "neutral";

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) return "neutral";

        Map<String, Integer> counter = new HashMap<>();
        counter.put("positive", 0);
        counter.put("negative", 0);
        counter.put("neutral", 0);

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String mapped = mapToSimple(sentiment);
            counter.put(mapped, counter.get(mapped) + 1);
        }

        return Collections.max(counter.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private String mapToSimple(String s) {
        if (s == null) return "neutral";
        s = s.toLowerCase();

        switch (s) {
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

    public List<ReviewRecord> loadDataset(String path) throws IOException {
        List<ReviewRecord> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] nextLine;
            boolean firstLine = true;
        
            while ((nextLine = reader.readNext()) != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
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