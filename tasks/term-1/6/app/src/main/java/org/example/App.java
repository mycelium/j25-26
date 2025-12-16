/*
 * K-Nearest Neighbors (KNN) Classifier Implementation
 * Laboratory Work #6
 */
package org.example;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        System.out.println("=== K-Nearest Neighbors (KNN) Classifier ===\n");

        try {
            // Generate training data
            DataGenerator dataGenerator = new DataGenerator();
            List<Point> trainingData = dataGenerator.generateSampleData(20); // 20 points per class

            System.out.println("Generated training data:");
            System.out.println("Total points: " + trainingData.size());
            for (Point point : trainingData) {
                System.out.println("  " + point);
            }
            System.out.println();

            // Create KNN classifier with k=3
            int k = 3;
            KNNClassifier classifier = new KNNClassifier(trainingData, k);
            System.out.println("Created KNN classifier with k=" + k);
            System.out.println("Training data size: " + classifier.getTrainingDataSize());
            System.out.println();

            // Generate test points
            List<Point> testPoints = dataGenerator.generateTestPoints(5);
            System.out.println("Generated test points:");
            for (int i = 0; i < testPoints.size(); i++) {
                System.out.println("  Test point " + (i + 1) + ": " + testPoints.get(i));
            }
            System.out.println();

            // Classify test points
            System.out.println("Classification results:");
            for (int i = 0; i < testPoints.size(); i++) {
                Point testPoint = testPoints.get(i);
                String prediction = classifier.classify(testPoint);
                System.out.printf("  Test point %d: %s -> Predicted class: %s%n",
                    i + 1, testPoint, prediction);
            }
            System.out.println();

            // Create plots
            PlotGenerator plotGenerator = new PlotGenerator();

            // Plot 1: Training data only
            plotGenerator.createTrainingDataPlot(trainingData, "training_data.png");

            // Plot 2: Training data + classified test points
            List<String> predictions = testPoints.stream()
                    .map(classifier::classify)
                    .toList();
            plotGenerator.createClassificationPlot(trainingData, testPoints, predictions,
                    "classification_results.png");

            System.out.println("Visualization plots created successfully!");
            System.out.println();

            // Interactive mode
            runInteractiveMode(classifier);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runInteractiveMode(KNNClassifier classifier) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Interactive Classification Mode ===");
        System.out.println("Enter coordinates (x y) to classify a point, or 'quit' to exit:");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            try {
                String[] parts = input.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Please enter two numbers separated by space.");
                    continue;
                }

                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);

                Point testPoint = new Point(x, y);
                String prediction = classifier.classify(testPoint);

                System.out.printf("Point (%.2f, %.2f) -> Predicted class: %s%n", x, y, prediction);

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter numeric coordinates.");
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }
}
