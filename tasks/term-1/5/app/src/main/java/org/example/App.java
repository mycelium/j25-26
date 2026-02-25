package org.example;

import org.example.processor.Processor;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        Processor processor = new Processor();
        try {
            processor.readReviews("../IMDB Dataset.csv");
        } catch (Exception e) {
            System.err.printf("Error while processing dataset: %s", e.toString());
        }
    }
}
