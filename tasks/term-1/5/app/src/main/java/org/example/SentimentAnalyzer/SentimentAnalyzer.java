package org.example.SentimentAnalyzer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class SentimentAnalyzer {
    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public String analyze(String text) {
        if (text == null || text.isBlank())
            return "neutral";

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        int totalScore = 0;
        int sentences = 0;

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String value = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            sentences++;

            switch (value.toLowerCase()) {
                case "very negative":
                    totalScore += 0;
                    break;
                case "negative":
                    totalScore += 1;
                    break;
                case "neutral":
                    totalScore += 2;
                    break;
                case "positive":
                    totalScore += 3;
                    break;
                case "very positive":
                    totalScore += 4;
                    break;
            }
        }

        int avg = totalScore / sentences;

        if (avg <= 1)
            return "negative";
        if (avg == 2)
            return "neutral";
        else
            return "positive";
    }
}
