package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class App {
    
    private StanfordCoreNLP pipeline;
    
    public App() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }
    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        
        try {
            String cleanText = text.replaceAll("<br\\s*/?>", " ")
                                  .replaceAll("<[^>]+>", " ")
                                  .replaceAll("\\s+", " ")
                                  .trim();
            
            Annotation document = new Annotation(cleanText);
            pipeline.annotate(document);
            
            int positiveCount = 0;
            int negativeCount = 0;
            int neutralCount = 0;
            
            for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                
                if (sentiment.contains("Positive")) {
                    positiveCount++;
                } else if (sentiment.contains("Negative")) {
                    negativeCount++;
                } else {
                    neutralCount++;
                }
            }
            
            if (positiveCount > negativeCount && positiveCount > neutralCount) {
                return "positive";
            } else if (negativeCount > positiveCount && negativeCount > neutralCount) {
                return "negative";
            } else {
                return "neutral";
            }
            
        } catch (Exception e) {
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            return "neutral";
        }
    }
    
    public void processReviews(String csvFilePath, int limit) {
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            int count = 0;
            int correct = 0;
            int total = 0;
            
            for (CSVRecord record : csvParser) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                
                String review = record.get("review");
                String actualSentiment = record.get("sentiment");
                String predictedSentiment = analyzeSentiment(review);
                
                System.out.println("\n=== Review " + (count + 1) + " ===");
                
                String shortReview = review.length() > 200 ? 
                    review.substring(0, 200) + "..." : review;
                System.out.println("Preview: " + shortReview.replaceAll("<br\\s*/?>", " ").replaceAll("<[^>]+>", ""));
                
                System.out.println("Actual: " + actualSentiment);
                System.out.println("Predicted: " + predictedSentiment);
                
                if (predictedSentiment.equals(actualSentiment)) {
                    correct++;
                    System.out.println("✓ CORRECT");
                } else {
                    System.out.println("✗ WRONG");
                }
                
                total++;
                count++;
            }
            
            System.out.println("\n=== RESULTS ===");
            System.out.println("Processed: " + total + " reviews");
            System.out.println("Correct: " + correct);
            System.out.println("Accuracy: " + String.format("%.2f%%", (correct * 100.0 / total)));
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Movie Review Sentiment Analysis ===");
        
        App app = new App();
        
        String csvPath = "IMDB Dataset.csv";
        
        app.processReviews(csvPath, 10);
    }
}
