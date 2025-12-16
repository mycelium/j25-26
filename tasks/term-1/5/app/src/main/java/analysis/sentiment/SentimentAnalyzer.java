package analysis.sentiment;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(properties);
    }

    public String sentimentAnalyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }

        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (sentences.isEmpty()) {
            return "neutral";
        }

        int[] votes = {0, 0, 0};

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            switch (sentiment) {
                case "Very negative":
                    votes[0]++;
                    break;

                case "Negative":
                    votes[0]++;
                    break;
                
                case "Neutral":
                    votes[1]++;
                    break;

                case "Positive":
                    votes[2]++;
                    break;

                case "Very positive":
                    votes[2]++;
                    break;
            
                default:
                    votes[1]++;
                    break;
            }
        }
        return determineVotes(votes);
    }

    private String determineVotes(int[] votes) {
        final int maximum = Math.max(votes[0], Math.max(votes[1], votes[2]));

        if (maximum == votes[0]) {
            return "negative";
        }
        else if (maximum == votes[1]) {
            return "neutral";
        }
        else if (maximum == votes[2]) {
            return "positive";
        }

        return null;
    }

}