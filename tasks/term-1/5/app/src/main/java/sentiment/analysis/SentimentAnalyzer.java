package sentiment.analysis;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> sentimentCount = new HashMap<>();
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String simpleSentiment = mapSentiment(sentiment);
            sentimentCount.put(simpleSentiment, sentimentCount.getOrDefault(simpleSentiment, 0) + 1);
        }

        if (sentimentCount.isEmpty()) {
            return "neutral";
        }

        Map.Entry<String, Integer> maxEntry = Collections.max(
                sentimentCount.entrySet(),
                Map.Entry.comparingByValue()
        );

        return maxEntry.getKey();
    }

    private String mapSentiment(String stanfordSentiment) {
        if (stanfordSentiment == null) {
            return "neutral";
        }

        switch (stanfordSentiment.toLowerCase()) {
            case "very positive":
            case "positive":
                return "positive";
            case "very negative":
            case "negative":
                return "negative";
            default:
                return "neutral";
        }
    }

    public void close() {
        if (pipeline != null) {
            pipeline = null;
            System.gc();
        }
    }
}