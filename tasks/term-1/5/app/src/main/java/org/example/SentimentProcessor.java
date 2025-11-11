package org.example;

import org.example.data.Review;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentimentProcessor {

    private final StanfordCoreNLP pipeline;

    public SentimentProcessor() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String processReview(Review review) {
        Annotation annotation = pipeline.process(review.getText());
        String overallSentiment = "neutral"; // Значение по умолчанию

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String sentenceSentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            // Для простоты берём тональность первого предложения.
            // В реальных приложениях можно учитывать все предложения.
            if (sentenceSentiment != null) {
                overallSentiment = sentenceSentiment.toLowerCase();
                break;
            }
        }
        return overallSentiment;
    }
}