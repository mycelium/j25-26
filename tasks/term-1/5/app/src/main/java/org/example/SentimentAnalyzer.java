package org.example;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;

public class SentimentAnalyzer {
    private static StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }

    public String predict(String text){
        if (text==null || text.isEmpty()) return "neutral";

        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

        int negativeVotes = 0;
        int neutralVotes = 0;
        int positiveVotes = 0;
        for (CoreMap sentence: sentences){ 
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class); 
            switch (sentiment) {
            case "Very negative":
            case "Negative":
                negativeVotes++;
                break;
            case "Very positive":
            case "Positive":
                positiveVotes++;
                break;
            default:
                neutralVotes++;
        }
        } 
        return determineByVotes(negativeVotes, neutralVotes, positiveVotes, sentences.size());
    }

    private String determineByVotes(int negative, int neutral, int positive, int total) {
    if (positive > negative && positive > neutral) return "positive";
    if (negative > positive && negative > neutral) return "negative";

    if (positive == neutral && positive > negative) {
        return "positive";
    }
    if (negative == neutral && negative > positive) {
        return "negative";
    }
    
    return "neutral";
    }

}
