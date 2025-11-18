package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class App {

    public static void main(String[] args) {

        String path = "app/src/main/resources/MiniDataset.txt"; //  IMDB Dataset.csv

        try {

            InputStream input = new FileInputStream(path);

            ReviewReader reader = new ReviewReader();
            List<String> reviews = reader.load(input);

            System.out.println("Loaded reviews: " + reviews.size());
            System.out.println();

            Sentiment analyzer = new Sentiment();

            int pos = 0, neg = 0, neu = 0;
            int index = 1;

            for (String r : reviews) {

                String cls = analyzer.classify(r);

                switch (cls) {
                    case "positive": pos++; break;
                    case "negative": neg++; break;
                    default: neu++; break;
                }

                if (index <= 10) {
                    System.out.println("[" + index + "] " + r);
                    System.out.println(" -> " + cls);
                    System.out.println();
                }

                index++;
            }

            System.out.println("Final analysis");
            System.out.println("Positive: " + pos);
            System.out.println("Negative: " + neg);
            System.out.println("Neutral: " + neu);

        } catch (Exception e) {
            System.out.println("Failed to read file: " + path);
            System.out.println(e.getMessage());
        }
    }
}
