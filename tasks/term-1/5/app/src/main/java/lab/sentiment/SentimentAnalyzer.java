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
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        if (text == null || text.isBlank()) return "Neutral";

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null || sentences.isEmpty()) return "Neutral";

        CoreMap sentence = sentences.get(0);
        return sentence.get(SentimentCoreAnnotations.SentimentClass.class);
    }

    public void processDataset(String inputPath, String outputPath) {
        System.out.println("Loading models and processing file: " + inputPath);

        try (CSVReader reader = new CSVReader(new FileReader(inputPath));
             CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {

            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return;

            String[] header = allRows.get(0);
            int reviewIdx = -1;

            //нахождение колонки с текстом
            for (int i = 0; i < header.length; i++) {
                if (header[i].toLowerCase().contains("review")) {
                    reviewIdx = i;
                    break;
                }
            }

            if (reviewIdx == -1) throw new Exception("Column 'review' not found");

            //подготовка заголовка
            String[] newHeader = Arrays.copyOf(header, header.length + 1);
            newHeader[newHeader.length - 1] = "predicted_sentiment";
            writer.writeNext(newHeader);

            //обработка всех строк файла
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                
                //удаление HTML тегов 
                String cleanText = row[reviewIdx].replaceAll("<[^>]*>", ""); 
                
                String sentiment = analyzeSentiment(cleanText);
                
                String[] newRow = Arrays.copyOf(row, row.length + 1);
                newRow[newRow.length - 1] = sentiment;
                writer.writeNext(newRow);
                
                //вывод прогресса каждые 10 строк
                if (i % 10 == 0 || i == allRows.size() - 1) {
                    System.out.println("Processed " + i + " / " + (allRows.size() - 1) + " rows...");
                }
            }

            System.out.println("Finished! Results in: " + outputPath);

        } catch (Exception e) {
            System.err.println("Error processing CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String input = (args.length > 0) ? args[0] : "testss.csv";
        String output = (args.length > 1) ? args[1] : "results.csv";

        new SentimentAnalyzer().processDataset(input, output);
    }
}
