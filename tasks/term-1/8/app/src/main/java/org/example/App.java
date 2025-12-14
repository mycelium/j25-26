package org.example;

public class App {
    public static void main(String[] args) {
        try {
            Classifier recognizer = new Classifier();
            recognizer.executeTrainingAndAssessment();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}