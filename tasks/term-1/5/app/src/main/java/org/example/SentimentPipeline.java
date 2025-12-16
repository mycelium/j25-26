package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

public class SentimentPipeline{

    private StanfordCoreNLP pipeline;

    public SentimentPipeline() {
        Properties pr = new Properties();
        pr.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(pr);
    }

    public String analyzeReviewText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty review.");
        }

        Annotation annotation   = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.isEmpty()) return "neutral";

        int pNumb = 0, negNumb = 0, neutNumb  = 0;
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            switch (sentiment.toLowerCase()) {
                case "very positive", "positive" -> pNumb++;
                case "very negative", "negative" -> negNumb++;
                default -> neutNumb++;
            }
        }

        if (pNumb >= negNumb && pNumb >= neutNumb) {
            return "positive";
        } else if (negNumb >= pNumb && negNumb >= neutNumb) {
            return "negative";
        } else {
            return "neutral";
        }
    }
}


