package org.example.processor;

import com.opencsv.CSVReader;
import org.example.analyzer.Analyzer;

import java.io.FileReader;
import java.util.List;

public class Processor {

    private final Analyzer analyzer = new Analyzer();

    public void readReviews(String filePath) throws Exception {

        System.out.println("Line number | predicted score | given score");

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {

            List<String[]> rows = reader.readAll();

            for (int i = 1; i < rows.size(); i++) { // пропускаем заголовок
                String[] row = rows.get(i);

                String predicted = analyzer.analyzeSentiment(row[0]);
                String actual = row[1];

                System.out.printf("%d | %s | %s%n", i, predicted, actual);
            }
        }
    }
}
