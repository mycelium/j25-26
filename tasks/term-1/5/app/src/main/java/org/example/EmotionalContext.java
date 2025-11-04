package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.util.*;

public class EmotionalContext
{
    private StanfordCoreNLP pipeline;

    public EmotionalContext()
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("coref.algorithm", "neural");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String BillingEmotional(String text)
    {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        try {
            Annotation annotation = pipeline.process(text);
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

            if (sentences.isEmpty())
            {
                return "neutral";
            }
            int positive = 0;
            int negative = 0;
            for(CoreMap sentence : sentences)
            {
                String emotion = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                if (emotion.contains("positive"))
                {
                    positive++;
                }
                else if (emotion.contains("negative"))
                {
                    negative++;
                }
            }
            int stats = positive - negative;

            if (stats > 0) {
                return "positive";
            } else if (stats < 0) {
                return "negative";
            } else {
                return "neutral";
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return "neutral";
        }
    }

}