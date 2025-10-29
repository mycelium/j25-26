package org.example;

import java.io.*;

public class Processor {
    private SentimentAnalyzer analyzer = new SentimentAnalyzer();

    public void process(InputStream input) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
        String line;
        int counter = 1;
        
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String review = line.trim();
            String predicted = analyzer.predict(review);

             System.out.println("Review " + counter + ": " + review);
            System.out.println("Predicted: " + predicted);
            System.out.println(" ");
            counter++;
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
