package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class Process {
    private final StanfordCoreNLP nlpPipeline;

    public Process() {
        Properties settings = new Properties();
        settings.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        settings.setProperty("ssplit.isOneSentence", "false");
        this.nlpPipeline = new StanfordCoreNLP(settings);
    }

    public String inferMood(String inputText) {
        var doc = new Annotation(inputText);
        nlpPipeline.annotate(doc);

        int posWeight = 0;
        int negWeight = 0;

        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            String label = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            if (label == null) continue;

            switch (label.toLowerCase()) {
                case "very positive", "positive" -> posWeight++;
                case "very negative", "negative" -> negWeight++;
            }
        }

        if (posWeight > negWeight) return "positive";
        if (negWeight > posWeight) return "negative";
        return "neutral";
    }
}