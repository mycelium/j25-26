package org.example.analyzer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

public class Analyzer {

    private static final StanfordCoreNLP PIPELINE = buildPipeline();

    private static StanfordCoreNLP buildPipeline() {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        properties.setProperty("tokenize.language", "en");
        return new StanfordCoreNLP(properties);
    }

    public String analyzeSentiment(String text) {
        if (isBlank(text)) {
            return "neutral";
        }

        Annotation annotation = PIPELINE.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        int totalScore = 0;

        for (CoreMap sentence : sentences) {
            totalScore += extractSentenceScore(sentence);
        }

        int averageScore = Math.round((float) totalScore / sentences.size());
        return mapScoreToLabel(averageScore);
    }

    private int extractSentenceScore(CoreMap sentence) {
        Tree sentimentTree = sentence.get(
                SentimentCoreAnnotations.SentimentAnnotatedTree.class
        );
        return RNNCoreAnnotations.getPredictedClass(sentimentTree);
    }

    private String mapScoreToLabel(int score) {
        switch (score) {
            case 0:
            case 1:
                return "negative";
            case 3:
            case 4:
                return "positive";
            default:
                return "neutral";
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
