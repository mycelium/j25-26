package org.example;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            String datasetPath = "src/main/resources/dataset/IMDB Dataset.csv"; 
            DatasetPreprocessing preprocessor = new DatasetPreprocessing();
            
            List<MovieReview> movieReviews = preprocessor.loadDataset(datasetPath);

            SentimentAnalyzer analyzer = new SentimentAnalyzer();
            System.out.println("Number of reviews " + movieReviews.size() +"\n");
            System.out.println("\n---===[Sentiment Analysis]===---");

            int correct = 0;
            int startReview = 0;
            int totalToShow = 10;  // чтобы проанализировать все отзывы, нужно заменить 10 на movieReviews.size(), но так как тогда анализ займет много времени (50000 отзывов), анализируются первые 10 отзывов
            
            for (int i = startReview; i < totalToShow; i++) {
                MovieReview review = movieReviews.get(i);
                
                 System.out.println("Review " + (i + 1) + ":");
                 System.out.println("Actual sentiment: " + review.getActualSentiment());
                 
                 String predictedSentiment = analyzer.analyzeSentiment(review.getProcessedText());
                 review.setCalculatedSentiment(predictedSentiment);
                System.out.println("Calculated sentiment: " + review.getCalculatedSentiment());
                System.out.println("Correct calculation: " + (review.isCalculationCorrect() ? "YES" : "NO"));
                System.out.println("---");
                
                if (review.isCalculationCorrect()) {
                    correct++;
                }
            }
            
          
            System.out.println("=== Result ===");
            System.out.println("Correct: " + correct + "/" + (totalToShow-startReview));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}