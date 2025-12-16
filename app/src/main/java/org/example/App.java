/*
 * K-Nearest Neighbors (KNN) Classifier Implementation
 * Laboratory Work #6
 */
package org.example;

import java.util.List;

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

            // Demo classification with specific points
            demonstrateClassification(classifier);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstrateClassification(KNNClassifier classifier) {
        System.out.println("=== Classification Demo ===");
        System.out.println("Testing classification with sample points:");

        // Test points that should fall into different classes
        Point[] demoPoints = {
            new Point(2.0, 2.0),   // Should be class A (near A cluster center)
            new Point(8.0, 3.0),   // Should be class B (near B cluster center)
            new Point(5.0, 8.0),   // Should be class C (near C cluster center)
            new Point(1.0, 1.0),   // Should be class A (within A cluster)
            new Point(6.0, 5.0)    // Border point, could vary
        };

        for (Point testPoint : demoPoints) {
            String prediction = classifier.classify(testPoint);
            System.out.printf("Point (%.1f, %.1f) -> Predicted class: %s%n",
                testPoint.getX(), testPoint.getY(), prediction);
        }

        System.out.println("\nDemo completed!");
    }
}
