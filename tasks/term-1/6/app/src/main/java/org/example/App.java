/*
 * Реализация классификатора K-ближайших соседей (KNN)
 * Лабораторная работа №6
 */
package org.example;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("=== K-Nearest Neighbors (KNN) Classifier ===\n");

        try {
            // Генерация обучающих данных
            DataGenerator dataGenerator = new DataGenerator();
            List<Point> trainingData = dataGenerator.generateSampleData(20); // 20 точек на класс

            System.out.println("Generated training data:");
            System.out.println("Total points: " + trainingData.size());
            for (Point point : trainingData) {
                System.out.println("  " + point);
            }
            System.out.println();

            // Создание KNN-классификатора с k = 3
            int k = 3;
            KNNClassifier classifier = new KNNClassifier(trainingData, k);
            System.out.println("Created KNN classifier with k=" + k);
            System.out.println("Training data size: " + classifier.getTrainingDataSize());
            System.out.println();

            // Генерация тестовых точек
            List<Point> testPoints = dataGenerator.generateTestPoints(5);
            System.out.println("Generated test points:");
            for (int i = 0; i < testPoints.size(); i++) {
                System.out.println("  Test point " + (i + 1) + ": " + testPoints.get(i));
            }
            System.out.println();

            // Классификация тестовых точек
            System.out.println("Classification results:");
            for (int i = 0; i < testPoints.size(); i++) {
                Point testPoint = testPoints.get(i);
                String prediction = classifier.classify(testPoint);
                System.out.printf("  Test point %d: %s -> Predicted class: %s%n",
                    i + 1, testPoint, prediction);
            }
            System.out.println();

            // Создание графиков
            PlotGenerator plotGenerator = new PlotGenerator();

            // График 1: только обучающие данные
            plotGenerator.createTrainingDataPlot(trainingData, "training_data.png");

            // График 2: обучающие данные + классифицированные тестовые точки
            List<String> predictions = testPoints.stream()
                    .map(classifier::classify)
                    .toList();
            plotGenerator.createClassificationPlot(trainingData, testPoints, predictions,
                    "classification_results.png");

            System.out.println("Visualization plots created successfully!");
            System.out.println();

            // Демонстрация классификации для конкретных точек
            demonstrateClassification(classifier);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstrateClassification(KNNClassifier classifier) {
        System.out.println("=== Classification Demo ===");
        System.out.println("Testing classification with sample points:");

        // Тестовые точки, которые должны относиться к разным классам
        Point[] demoPoints = {
            new Point(2.0, 2.0),   // Должен быть класс A (близко к центру кластера A)
            new Point(8.0, 3.0),   // Должен быть класс B (близко к центру кластера B)
            new Point(5.0, 8.0),   // Должен быть класс C (близко к центру кластера C)
            new Point(1.0, 1.0),   // Должен быть класс A (внутри кластера A)
            new Point(6.0, 5.0)    // Пограничная точка, результат может отличаться
        };

        for (Point testPoint : demoPoints) {
            String prediction = classifier.classify(testPoint);
            System.out.printf("Point (%.1f, %.1f) -> Predicted class: %s%n",
                testPoint.getX(), testPoint.getY(), prediction);
        }

        System.out.println("\nDemo completed!");
    }
}
