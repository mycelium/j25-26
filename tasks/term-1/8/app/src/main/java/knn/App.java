package knn;

import org.example.NumberClassifier;

public class App {
    public static void main(String[] args) {
        try {
            NumberClassifier classifier = new NumberClassifier();
            classifier.trainAndEvaluate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}