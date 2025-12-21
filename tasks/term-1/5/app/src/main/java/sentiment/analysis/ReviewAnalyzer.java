package sentiment.analysis;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import java.util.*;

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

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }

        Map<String, Integer> counts = new HashMap<>();
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            String simple = mapToSentiment(sentiment);
            counts.put(simple, counts.getOrDefault(simple, 0) + 1);
        }

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
    }

    private String mapToSentiment(String coreNLPSentiment) {
        if (coreNLPSentiment == null) return "neutral";
        switch (coreNLPSentiment.toLowerCase()) {
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

    public void analyzeReview(Review review) {
        String sentiment = analyzeSentiment(review.getText());
        review.setCalculatedSentiment(sentiment);
    }
}
