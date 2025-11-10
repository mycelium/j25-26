package org.example;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.sentiment.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import com.opencsv.*;

public class SentimentAnalyzer {
    private StanfordCoreNLP pipeline;
    
    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        this.pipeline = new StanfordCoreNLP(props);
    }
    
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        
        if (sentences == null || sentences.isEmpty()) {
            return "neutral";
        }
        
        Map<String, Integer> sentimentCount = new HashMap<>();
        
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            sentimentCount.put(sentiment, sentimentCount.getOrDefault(sentiment, 0) + 1);
        }
        
        return sentimentCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
    }
    
    private File getProjectRoot() {
        //для корня проекта
        File currentDir = new File(System.getProperty("user.dir"));
        System.out.println("Current working directory: " + currentDir.getAbsolutePath());
        
        //для папки app
        if (currentDir.getName().equals("app")) {
            return currentDir.getParentFile();
        }
        return currentDir;
    }
    
    public void analyzeCSVDataset(String inputPath, String outputPath) {
        CSVReader reader = null;
        CSVWriter writer = null;
        
        try {
            File projectRoot = getProjectRoot();
            File inputFile = new File(projectRoot, inputPath);
            File outputFile = new File(projectRoot, outputPath);
            
            System.out.println("Input file: " + inputFile.getAbsolutePath());
            System.out.println("Results saved to: " + outputFile.getAbsolutePath());
            
            if (!inputFile.exists()) {
                System.err.println("file not found!");
                
            }
            
            System.out.println("Reading dataset...");
            
            reader = new CSVReader(new FileReader(inputFile));
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                System.err.println("file is empty!");
                return;
            }
            
            //колонка с отзывом
            String[] header = allRows.get(0);
            int reviewColumn = -1;
            
            for (int i = 0; i < header.length; i++) {
                String col = header[i].toLowerCase();
                if (col.contains("review") || col.contains("text")) {
                    reviewColumn = i;
                    break;
                }
            }
            
            if (reviewColumn == -1) {
                System.err.println("no review column!");
                return;
            }
            
            System.out.println("Review column: " + header[reviewColumn]);
            System.out.println("Total rows: " + (allRows.size() - 1));
            
            writer = new CSVWriter(new FileWriter(outputFile));
            
            //ожидаемый результат
            String[] newHeader = Arrays.copyOf(header, header.length + 1);
            newHeader[newHeader.length - 1] = "predicted_sentiment";
            
            List<String[]> results = new ArrayList<>();
            results.add(newHeader);
            
            int processed = 0;
            // Process only first 10 rows for quick test
            for (int i = 1; i < allRows.size() && i <= 10; i++) {
                String[] row = allRows.get(i);
                
                if (row.length <= reviewColumn || row[reviewColumn] == null) {
                    continue;
                }
                
                String review = row[reviewColumn].trim();
                if (review.isEmpty()) {
                    continue;
                }
                
                String predictedSentiment = analyzeSentiment(review);
                
                review = review.replaceAll("<br\\s*/?>", " ").replaceAll("<[^>]+>", " ");
               
                
                String[] newRow = Arrays.copyOf(row, row.length + 1);
                newRow[newRow.length - 1] = predictedSentiment;
                results.add(newRow);
                
                processed++;
              
            }
            
            writer.writeAll(results);
            
            
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                System.err.println("Error closing files: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        String inputFile = "IMDB Dataset.csv";
        String outputFile = "results.csv";
        
        if (args.length >= 1) inputFile = args[0];
        if (args.length >= 2) outputFile = args[1];
        
        
        SentimentAnalyzer analyzer = new SentimentAnalyzer();
        analyzer.analyzeCSVDataset(inputFile, outputFile);
    }
}
