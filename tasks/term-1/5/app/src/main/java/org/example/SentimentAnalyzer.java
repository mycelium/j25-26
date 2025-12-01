package org.example;

import edu.stanford.nlp.pipeline.*;
import java.util.*;

public class SentimentAnalyzer {
    StanfordCoreNLP pipeline;
    
    public SentimentAnalyzer() {
        Properties p = new Properties();
        p.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        pipeline = new StanfordCoreNLP(p);
    }
    
    public String analyze(String text) {
        if (text == null || text.isEmpty()) return "neutral";
        
        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);
        
        String result = doc.toString();
        if (result.contains("Positive")) return "positive";
        if (result.contains("Negative")) return "negative";
        return "neutral";
    }
}
