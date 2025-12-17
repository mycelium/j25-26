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
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class App {
    
    private final StanfordCoreNLP pipeline;
    
    public App() {
        Properties props = new Properties();
	     props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
	     props.setProperty("tokenize.language", "en");
	     props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
	     props.setProperty("sentiment.model", "edu/stanford/nlp/models/sentiment/sentiment.ser.gz");
	     props.setProperty("coref.algorithm", "neural"); 
        
        this.pipeline = new StanfordCoreNLP(props);
    }
    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        
        try {
            String cleanText = text.replaceAll("<br\\s*/?>", " ")
                                  .replaceAll("\\s+", " ")
                                  .trim();
            
            if (cleanText.isEmpty()) {
                return "neutral";
            }
            
            Annotation document = new Annotation(cleanText);
            pipeline.annotate(document);
            
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            
            if (sentences.isEmpty()) {
                return "neutral";
            }
            
            int positiveCount = 0;
            int negativeCount = 0;
            int neutralCount = 0;
            
            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                
                switch (sentiment) {
                    case "Very positive":
                    case "Positive":
                        positiveCount++;
                        break;
                    case "Very negative":
                    case "Negative":
                        negativeCount++;
                        break;
                    case "Neutral":
                        neutralCount++;
                        break;
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
    
    public void processReviews(String csvFilePath, int limit) throws IOException {
        String absolutePath = Paths.get(csvFilePath).toAbsolutePath().toString();
        System.out.println("Reading file: " + absolutePath);
        
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withQuote('"')
                     .withEscape('\\'))) {
            
            int count = 0;
            int correct = 0;
            int total = 0;
            
            System.out.println("\n=== Processing Reviews ===");
            
            for (CSVRecord record : csvParser) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                
                String review = record.get("review");
                String actualSentiment = record.get("sentiment");
                String predictedSentiment = analyzeSentiment(review);
                
                System.out.println("\n=== Review " + (count + 1) + " ===");
                
                String shortReview = review.length() > 150 ? 
                    review.substring(0, 150) + "..." : review;
                System.out.println("Preview: " + shortReview
                    .replaceAll("<br\\s*/?>", " ")
                    .replaceAll("\\s+", " ")
                    .trim());
                
                System.out.println("Actual:    " + actualSentiment);
                System.out.println("Predicted: " + predictedSentiment);
                
                boolean isCorrect = predictedSentiment.equals(actualSentiment.toLowerCase());
                if (isCorrect) {
                    correct++;
                    System.out.println("✓ CORRECT");
                } else {
                    System.out.println("✗ WRONG");
                }
                
                total++;
                count++;
            }
            
            System.out.println("\n=== FINAL RESULTS ===");
            System.out.println("Total processed: " + total + " reviews");
            System.out.println("Correct predictions: " + correct);
            System.out.println("Accuracy: " + String.format("%.2f%%", (correct * 100.0 / total)));
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Movie Review Sentiment Analysis ===\n");
        
        App analyzer = new App();
        
        String csvPath = "IMDB Dataset.csv";
        
        try {
            analyzer.processReviews(csvPath, 20);
        } catch (IOException e) {
            System.err.println("Failed to process reviews: " + e.getMessage());
            System.err.println("Please make sure the file 'IMDB Dataset.csv' exists in the current directory.");
            System.err.println("You can download it from: https://drive.google.com/file/d/15oxF9_ifxKMBs56eUIaziD4nRH3VUV9E");
            System.exit(1);
        }
    }
}
