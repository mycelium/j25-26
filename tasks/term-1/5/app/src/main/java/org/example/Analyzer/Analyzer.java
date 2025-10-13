package org.example.Analyzer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;

public class Analyzer {
    private StanfordCoreNLP pipeline;
    
    public Analyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        this.pipeline = new StanfordCoreNLP(props);
    }
    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        
        Annotation annotation = pipeline.process(text);
        double sentimentScore = 0;
        int sentenceCount = 0;
        
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            
            sentimentScore += sentiment;
            sentenceCount++;
        }
        
        if (sentenceCount == 0) {
            return "neutral";
        }
        
        int averageScore = (int) (sentimentScore / sentenceCount + 0.5);
        return classifySentiment(averageScore);
    }
    
    private String classifySentiment(int score) {
        if (score < 2) {
            return "negative";
        } else if (score > 2) {
            return "positive";
        } else {
            return "neutral";
        }
    }
}