package org.example.processor;

import java.io.FileReader;

import org.example.analyzer.Analyzer;

import com.opencsv.CSVReader;

public class Processor
{
    private Analyzer analyzer = new Analyzer();

    public void readReviews(String filePath) throws Exception{

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            
            String[] reviewSentimentLine;
            reader.skip(1);
            Integer lineNumber = 1;
            System.out.println("Line number | predicted score | given score");
            while ((reviewSentimentLine = reader.readNext()) != null) {

                String predictedStatement = analyzer.analyzeSentiment(reviewSentimentLine[0]);
                System.out.printf("%d | %s | %s\n", lineNumber++, predictedStatement, reviewSentimentLine[1]);
            }
        } 
    }
}
