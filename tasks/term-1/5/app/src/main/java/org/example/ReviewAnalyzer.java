package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.Tree;

import java.util.List;
import java.util.Properties;

public class ReviewAnalyzer {
    private final StanfordCoreNLP pipeline;

    public ReviewAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        int totalSentiment = 0;
        int count = 0;

        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            if (tree != null) {
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                totalSentiment += sentiment;
                count++;
            }
        }

        if (count == 0) {
            return "neutral";
        }

        double average = totalSentiment / (double) count;

        if (average <= 1.5) {
            return "negative";
        } else if (average >= 2.5) {
            return "positive";
        } else {
            return "neutral";
        }
    }

    public void analyzeReview(Review review) {
        String sentiment = analyzeSentiment(review.getText());
        review.setSentiment(sentiment);
    }
}
