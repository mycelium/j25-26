package org.example;

import edu.stanford.nlp.pipeline.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class App {
    public static void main(String[] args) {
        System.out.println(" Starting movie reviews sentiment analysis...");
        
        //  Configuration CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        try (CSVReader reader = new CSVReader(new FileReader("review.csv"));
             CSVWriter writer = new CSVWriter(new FileWriter("results.csv"))) {
            
            String[] nextLine;
            int lineCount = 0;
            int analyzedCount = 0;
            
            String[] header = {"Review_Text", "Sentiment"};
            writer.writeNext(header);
            reader.readNext();
            
            // Analyze each review
            while ((nextLine = reader.readNext()) != null) {
                lineCount++;
                
                if (nextLine.length >= 2) {
                    String reviewText = nextLine[1].trim();    
                    if (!reviewText.isEmpty()) {
                        CoreDocument document = new CoreDocument(reviewText);
                        pipeline.annotate(document);
                        String overallSentiment = getDominantSentiment(document);
                        String[] resultLine = {reviewText, overallSentiment};
                        writer.writeNext(resultLine);
                        if (analyzedCount % 50 == 0) {
                            System.out.println("Processed " + analyzedCount + " reviews...");
                        }
                        
                        analyzedCount++;
                    }
                }
            }
            
            System.out.println(" Analysis completed!");
            System.out.println(" " + analyzedCount + " reviews analyzed out of " + lineCount + " lines");
            System.out.println(" Results saved to: data/sentiment_results.csv");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getDominantSentiment(CoreDocument document) {
        // Count sentiments from each sentence
        Map<String, Integer> sentimentCount = new HashMap<>();
        
        for (var sentence : document.sentences()) {
            String sent = sentence.sentiment();
            sentimentCount.put(sent, sentimentCount.getOrDefault(sent, 0) + 1);
        }
     
        return sentimentCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }
}