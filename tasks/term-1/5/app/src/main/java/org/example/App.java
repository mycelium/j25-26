package org.example;

import org.example.dataset.DatasetReader;
import org.example.dataset.Review;
import org.example.evaluation.EvaluationResult;
import org.example.evaluation.SentimentEvaluator;
import org.example.sentiment.SentimentAnalyzer;

import java.util.List;

public class App
{
    private static final int MAX_REVIEWS_TO_ANALYZE = 5;

    public static void main(String[] args) throws Exception
    {
        String datasetPath = "../data/IMDB Dataset.csv";

        if (args.length > 0)
            datasetPath = args[0];

        List<Review> reviews = DatasetReader.read(datasetPath);
        System.out.println("Loaded reviews: " + reviews.size());

        int limit = MAX_REVIEWS_TO_ANALYZE > 0 ? Math.min(MAX_REVIEWS_TO_ANALYZE, reviews.size()) : reviews.size();

        List<Review> reviewsToAnalyze = reviews.subList(0, limit);
        System.out.println("Reviews used for analysis: " + reviewsToAnalyze.size());

        SentimentAnalyzer analyzer = new SentimentAnalyzer();
        List<String> predictions = analyzer.predictAll(reviewsToAnalyze);

        EvaluationResult result = SentimentEvaluator.evaluate(reviewsToAnalyze, predictions);
        result.print();
    }
}