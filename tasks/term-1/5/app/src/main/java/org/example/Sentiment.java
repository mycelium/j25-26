package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class Sentiment {

    private final StanfordCoreNLP nlp;

    public Sentiment() {

        Properties cfg = new Properties();
        cfg.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        cfg.setProperty("tokenize.language", "en");

        this.nlp = new StanfordCoreNLP(cfg);
    }

    public String classify(String text) {

        Annotation doc = new Annotation(text);
        nlp.annotate(doc);

        int neg = 0, neu = 0, pos = 0;

        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {

            String mood = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            if (mood.contains("Negative")) neg++;
            else if (mood.contains("Positive")) pos++;
            else neu++;
        }

        if (pos > neg && pos >= neu) return "positive";
        if (neg > pos && neg >= neu) return "negative";
        return "neutral";
    }
}
