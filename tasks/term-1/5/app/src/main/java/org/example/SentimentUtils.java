package org.example;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.opencsv.CSVReader;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;


public class SentimentUtils {
    
    private final StanfordCoreNLP pipeline;
    
    public SentimentUtils() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }
    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        String cleaned = text.replaceAll("<br\\s*/?>", " ").trim();
        
        Annotation annotation = pipeline.process(cleaned);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }
        
        Map<String, Integer> counter = new HashMap<>();
        counter.put("positive", 0);
        counter.put("negative", 0);
        counter.put("neutral", 0);
        
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String simple = mapToSimple(sentiment);
            counter.put(simple, counter.get(simple) + 1);
        }
        
        return Collections.max(counter.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
    
    private String mapToSimple(String s) {
        if (s == null) return "neutral";
        s = s.toLowerCase();
        
        if (s.contains("positive")) {
            return "positive";
        } else if (s.contains("negative")) {
            return "negative";
        } else {
            return "neutral";
        }
    }
    
    public List<Review> loadReviews(String path) throws Exception {
        List<Review> list = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] line;
            boolean firstLine = true;
            
            while ((line = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                if (line.length >= 2) {
                    String text = line[0];
                    String label = line[1].toLowerCase();
                    list.add(new Review(text, label));
                }
            }
        }
        
        return list;
    }
    
    public static class Review {
        public final String text;
        public final String label;
        
        public Review(String text, String label) {
            this.text = text;
            this.label = label;
        }
    }
}