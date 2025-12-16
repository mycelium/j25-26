package sentiment.analysis;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
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

    public MovieReview analyzeSentiment (String text) {
        if (text == null || text.trim().isEmpty()) {
            return new MovieReview(text, "neutral");
        }

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences == null || sentences.isEmpty()) {
            return new MovieReview(text, "neutral");
        }

        CoreMap sentance = sentences.get(0);
        String sentiment = sentance.get(SentimentCoreAnnotations.SentimentClass.class);

        MovieReview review = new MovieReview(text);
        review.setSentiment(mapSentiment(sentiment));

        return review;
    }

    private String mapSentiment (String stanfordSentiment) {
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
