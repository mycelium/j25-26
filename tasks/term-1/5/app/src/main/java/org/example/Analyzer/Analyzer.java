package org.example.Analyzer;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Analyzer 
{
    private StanfordCoreNLP pipeline;

    public Analyzer()
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }

    public AnalyzerResult analyzeReview(String text) throws IllegalArgumentException, Exception
    {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Got empty text");
        }

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.isEmpty()) {
            throw new Exception("Null");
        }

        return getOverallSentiment(sentences, text);
    }

    public String getSimpleSentiment(String text) throws IllegalArgumentException, Exception
    {
        AnalyzerResult result = analyzeReview(text);
        String detailed = result.getSentiment();
        
        if (detailed.contains("positive")) return "positive";
        if (detailed.contains("negative")) return "negative";

        return "neutral";
    }

    private AnalyzerResult getOverallSentiment(List<CoreMap> sentences, String text)
    {
        int totalSentiment = 0;
        
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            totalSentiment += sentiment;
        }
        
        int averageSentiment = sentences.size() > 0 ? totalSentiment / sentences.size() : 2;
        return new AnalyzerResult(getSentimentLabel(averageSentiment), averageSentiment, text);
    }

    private String getSentimentLabel(int sentimantScore)
    {
        switch (sentimantScore) {
            case 0: return "very negative";
            case 1: return "negative";
            case 2: return "neutral";
            case 3: return "positive";
            case 4: return "very positive";
            default: return "neutral";
        }
    }

}
