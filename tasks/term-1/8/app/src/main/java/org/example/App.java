/*
 * MNIST Image Classification with Convolutional Neural Network using DeepLearning4j
 */
package org.example;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        System.out.println("=== MNIST CNN Classification ===");

        try {
            MNISTClassifier classifier = new MNISTClassifier();

            System.out.println("Building CNN model...");
            classifier.buildModel();

            System.out.println("Training model on MNIST dataset...");
            classifier.train();

            System.out.println("Evaluating model performance...");
            classifier.evaluate();

            System.out.println("=== Training and evaluation completed ===");

        } catch (IOException e) {
            System.err.println("Error during training/evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
