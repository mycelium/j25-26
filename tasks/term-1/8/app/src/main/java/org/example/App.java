package org.example;

import java.io.File;

class App {
    private static final String MODEL_PATH = "mnist-model.zip";
    public static void main(String[] args) {
        ImageClassifier classifier = new ImageClassifier();
        try {
            if (new File(MODEL_PATH).exists()) {
                classifier.loadModel(MODEL_PATH);
            } else {
                classifier.trainModel();
                classifier.saveModel(MODEL_PATH);
            }
            classifier.recognizeFromFile(classifier, "one.png");
            classifier.recognizeFromFile(classifier, "two.png");
            classifier.recognizeFromFile(classifier, "three.png");
            classifier.recognizeFromFile(classifier, "four.png");
            classifier.recognizeFromFile(classifier, "five.png");
            classifier.recognizeFromFile(classifier, "six.png");
            classifier.recognizeFromFile(classifier, "seven.png");
            classifier.recognizeFromFile(classifier, "eight.png");
            classifier.recognizeFromFile(classifier, "nine.png");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
