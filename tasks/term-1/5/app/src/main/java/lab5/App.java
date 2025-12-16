package lab5;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import java.util.*;
import java.io.*;

public class App {
    public static void main(String[] args) {
        
        Properties props = new Properties();
        
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        System.out.println("Initializing Stanford CoreNLP pipeline...");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        List<String> reviews = new ArrayList<>();
        File file = new File(args.length > 0 ? args[0] : "short_reviews.txt");
        
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    br.readLine(); 
                    
                    int count = 0;
                    while ((line = br.readLine()) != null && count < 20) {
                        
                        int firstQuote = line.indexOf('"');
                        int lastQuote = line.lastIndexOf('"');
                        
                        if (firstQuote >= 0 && lastQuote > firstQuote) {
                             String reviewText = line.substring(firstQuote + 1, lastQuote);
                             reviewText = reviewText.replace("\"\"", "\"");
                             reviews.add(reviewText);
                        } else {
                             reviews.add(line);
                        }
                        count++;
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                    return;
                }
            } else {
                System.err.println("File not found: " + file.getPath());
                System.out.println("Make sure 'short_reviews.txt' is in the current directory or provide a valid path.");
            }
        
        if (reviews.isEmpty()) {
            System.out.println("No reviews found or processed.");
        }

        for (String review : reviews) {
            if (review.trim().isEmpty()) continue;
            
            Annotation annotation = new Annotation(review);
            pipeline.annotate(annotation);

            
            System.out.println("Review: " + review);
            
            String mainSentiment = "neutral";
            int longestSentenceLength = 0;

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                String text = sentence.toString();
                if (text.length() > longestSentenceLength) {
                    longestSentenceLength = text.length();
                    mainSentiment = sentiment;
                }
            }
            
            String simplified = switch(mainSentiment) {
                case "Very positive", "Positive" -> "positive";
                case "Very negative", "Negative" -> "negative";
                default -> "neutral";
            };
            
            System.out.println("Rating: " + simplified);
            System.out.println("---");
        }
    }
}

