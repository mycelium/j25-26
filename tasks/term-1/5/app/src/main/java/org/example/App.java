package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class App {
    public static class SentimentAnalyzer {
        private final StanfordCoreNLP pipeline;

        public SentimentAnalyzer() {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
            this.pipeline = new StanfordCoreNLP(props);
        }

        public String getSentiment(String text) {
            if (text == null || text.trim().isEmpty()) {
                return "neutral";
            }

            var annotation = new edu.stanford.nlp.pipeline.Annotation(text);
            pipeline.annotate(annotation);

            int total = 0;
            int count = 0;

            var sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences == null) return "neutral";

            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                switch (sentiment) {
                    case "Very negative":
                    case "Negative":
                        total -= 1;
                        break;
                    case "Very positive":
                    case "Positive":
                        total += 1;
                        break;
                    case "Neutral":
                    default:
                        break;
                }
                count++;
            }

            if (count == 0) return "neutral";

            double avg = (double) total / count;
            if (avg > 0.1) return "positive";
            if (avg < -0.1) return "negative";
            return "neutral";
        }
    }

    public static void main(String[] args) {
        String inputCsv = "IMDB.csv";
        SentimentAnalyzer analyzer = new SentimentAnalyzer();

        try (CSVReader reader = new CSVReader(new FileReader(inputCsv))) {
            String[] header = reader.readNext(); 
            if (header == null) {
                System.err.println("CSV file is empty");
                return;
            }

            String[] row;
            int count = 0;

            while ((row = reader.readNext()) != null) {
                if (row.length == 0) continue;

                String review = row[0].trim();
                String sentiment = analyzer.getSentiment(review);
                System.out.printf(sentiment + "\n");

               
            }


        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.err.println("CSV format error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
