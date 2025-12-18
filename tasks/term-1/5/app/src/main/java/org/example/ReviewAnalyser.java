package org.example;

import com.opencsv.exceptions.CsvValidationException;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import com.opencsv.CSVReader;
import java.util.*;
import java.io.*;

public class ReviewAnalyser {
    private StanfordCoreNLP ppl;
    public ReviewAnalyser() {
        try {
            Properties prps = new Properties();
            prps.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
            prps.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
            prps.setProperty("sentiment.model", "edu/stanford/nlp/models/sentiment/sentiment.ser.gz");
            this.ppl = new StanfordCoreNLP(prps);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private int convertToScore(String sentiment) {
        if (sentiment == null)
        {
            return 2;
        }
        switch (sentiment.toLowerCase()) {
            case "very positive":
                return 5;
            case "positive":
                return 3;
            case "negative":
                return -2;
            case "very negative":
                return -3;
            default:
                return 0;
        }
    }
    public String Analyse(String text)
    {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        Annotation doc = new Annotation(text);
        ppl.annotate(doc);
        List<CoreMap> sntsList = doc.get(CoreAnnotations.SentencesAnnotation.class);
        if (sntsList.isEmpty())
        {
            return "neutral";
        }
        int sntScore = 0;
        for (CoreMap snt : sntsList)
        {
            String sentiment = snt.get(SentimentCoreAnnotations.SentimentClass.class);
            int score = convertToScore(sentiment);
            sntScore += score;
        }
        double avgScore = (double)sntScore / sntsList.size();
        if (avgScore < -0.3) return "negative";
        else if (avgScore > 0.3) return "positive";
        return "neutral";
    }
    public List<Review> loadReviews(String fPath) throws IOException {
        List<Review> reviews = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fPath))) {
            String[] line;
            int lineNumber = 0;
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    continue;
                }
                if (line.length >= 2) {
                    String text = line[0].trim();
                    String actualLabel = line[1].trim().toLowerCase();
                    reviews.add(new Review(text, actualLabel));
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return reviews;
    }
    public record Review(String text, String actualSentiment) {
    }
}



