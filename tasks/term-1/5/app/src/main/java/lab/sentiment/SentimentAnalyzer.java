package lab.sentiment;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.sentiment.*;
import edu.stanford.nlp.util.*;
import com.opencsv.*;

import java.util.*;
import java.io.*;

public class SentimentAnalyzer {
    private final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        props.setProperty("tokenize.language", "en");
        props.setProperty("quiet", "true"); 
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.isBlank()) return "Neutral";

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) return "Neutral";

        double totalScore = 0;
        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            totalScore += sentimentToScore(sentiment);
        }

        // среднее значение тональности
        double averageScore = totalScore / sentences.size();

        if (averageScore > 2.0) return "Positive";
        if (averageScore < 2.0) return "Negative";
        return "Neutral";
    }

    private int sentimentToScore(String sentiment) {
        if (sentiment == null) return 2;
        String s = sentiment.trim();
        // в Stanford 5 типов, перевод в баллы 0-4
        if (s.equalsIgnoreCase("Very Positive")) return 4;
        if (s.equalsIgnoreCase("Positive"))      return 3;
        if (s.equalsIgnoreCase("Neutral"))       return 2;
        if (s.equalsIgnoreCase("Negative"))      return 1;
        if (s.equalsIgnoreCase("Very Negative")) return 0;
        return 2;
    }

    public void processDataset(String inputPath, String outputPath) {
        System.out.println("Processing: " + inputPath + " -> " + outputPath);

        try (CSVReader reader = new CSVReader(new FileReader(inputPath));
             CSVWriter writer = new CSVWriter(new FileWriter(outputPath),
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.DEFAULT_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return;

            String[] header = allRows.get(0);
            int reviewIdx = -1;

            for (int i = 0; i < header.length; i++) {
                if (header[i].toLowerCase().contains("review")) {
                    reviewIdx = i;
                    break;
                }
            }

            if (reviewIdx == -1) throw new Exception("Column 'review' not found");

            String[] newHeader = Arrays.copyOf(header, header.length + 1);
            newHeader[newHeader.length - 1] = "predicted_sentiment";
            writer.writeNext(newHeader);

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                
                // очистка от HTML и переносов
                String cleanText = row[reviewIdx].replaceAll("<[^>]*>", " ").replace("\n", " ").replace("\r", " "); 
                
                String sentiment = analyzeSentiment(cleanText);
                
                String[] newRow = Arrays.copyOf(row, row.length + 1);
                newRow[newRow.length - 1] = sentiment;
                writer.writeNext(newRow);
                
                if (i % 10 == 0 || i == allRows.size() - 1) {
                    System.out.println("Processed " + i + " / " + (allRows.size() - 1) + " rows...");
                }
            }
            System.out.println("Done!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String input = (args.length > 0) ? args[0] : "testss.csv";
        String output = (args.length > 1) ? args[1] : "results.csv";
        new SentimentAnalyzer().processDataset(input, output);
    }
}