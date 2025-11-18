package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import java.util.Properties;
import java.util.List;
import java.util.Iterator;

public class SentimentAnalyzer
{
    private StanfordCoreNLP pipeline;

    public SentimentAnalyzer()
    {
        initializePipeline();
    }

    private void initializePipeline()
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String review)
    {
        try
        {
            Annotation annotation = new Annotation(review);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

            if (sentences == null || sentences.isEmpty())
            {
                throw new IllegalStateException("No sentences could be extracted from the review");
            }

            // Deleting elements with only one dot
            Iterator<CoreMap> iterator = sentences.iterator();
            while (iterator.hasNext())
            {
                CoreMap sentence = iterator.next();
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                if (sentenceText.trim().equals("."))
                {
                    iterator.remove();
                }
            }

            if (sentences.isEmpty())
            {
                throw new IllegalStateException("Only sentences consisting of a single point were extracted");
            }

            int positiveCount = 0;
            int negativeCount = 0;

            for (CoreMap sentence : sentences)
            {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

                int score = convertSentimentToScore(sentiment);

                if (convertScoreToPredictedSentiment(score).equals("very negative") ||
                        convertScoreToPredictedSentiment(score).equals("negative"))
                {
                    negativeCount++;
                }
                else
                {
                    positiveCount++;
                }

            }

            // We return negative if there are more negative suggestions.
            return negativeCount > positiveCount ? "negative" : "positive";

        }
        catch (Exception e)
        {
            System.err.println("Sentiment analysis error: " + e.getMessage());
            throw new RuntimeException("Sentiment analysis failed: " + e.getMessage(), e);
        }
    }

    private int convertSentimentToScore(String sentiment)
    {
        if (sentiment == null) return 2;

        switch (sentiment.toLowerCase())
        {
            case "very negative": return 0;
            case "negative": return 1;
            case "neutral": return 2;
            case "positive": return 3;
            case "very positive": return 4;
            default: return 2;
        }
    }

    private String convertScoreToPredictedSentiment(int score)
    {
        return score <= 1 ? "negative" : "positive";
    }
}