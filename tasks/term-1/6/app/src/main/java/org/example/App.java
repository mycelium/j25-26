package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        try {
            System.out.println("Starting KNN Classifier...");
            
            //классификатор с k=3
            KNNClassifier classifier = new KNNClassifier(3);

            //тренировочные данные для трех классов
            List<Point> trainingData = generateSampleData();
            classifier.addTrainingPoints(trainingData);

            System.out.println("Training data generated: " + trainingData.size() + " points");

            //график тренировочных данных
            ChartGenerator.generateTrainingDataChart(trainingData, "training_data.png");

            testClassifier(classifier);

            System.out.println("\nAll charts have been generated as PNG files!");
            System.out.println("Check the project folder for:");
            System.out.println("- training_data.png");
            System.out.println("- classification_*.png files");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Point> generateSampleData() {
        List<Point> data = new ArrayList<>();
        Random random = new Random(42);

        //класс A - точки вокруг (2, 2)
        for (int i = 0; i < 20; i++) {
            double x = 2 + random.nextGaussian() * 0.5;
            double y = 2 + random.nextGaussian() * 0.5;
            data.add(new Point(x, y, "A"));
        }

        //класс B - точки вокруг (5, 5)
        for (int i = 0; i < 20; i++) {
            double x = 5 + random.nextGaussian() * 0.5;
            double y = 5 + random.nextGaussian() * 0.5;
            data.add(new Point(x, y, "B"));
        }

        //класс C - точки вокруг (8, 2)
        for (int i = 0; i < 20; i++) {
            double x = 8 + random.nextGaussian() * 0.5;
            double y = 2 + random.nextGaussian() * 0.5;
            data.add(new Point(x, y, "C"));
        }

        return data;
    }

    private static void testClassifier(KNNClassifier classifier) {
        Point[] testPoints = {
            new Point(2.5, 2.5, "?"),
            new Point(5.0, 5.0, "?"),
            new Point(7.5, 2.0, "?"),
            new Point(4.0, 4.0, "?"),
            new Point(3.0, 3.0, "?")
        };

        System.out.println("\n=== K-Nearest Neighbors Classification ===");
        System.out.println("Training data size: " + classifier.getTrainingData().size());
        System.out.println("K value: 3");
        System.out.println();

        for (Point testPoint : testPoints) {
            String predictedClass = classifier.classify(testPoint);
            Point classifiedPoint = new Point(testPoint.getX(), testPoint.getY(), predictedClass);
            
            System.out.printf("Point (%.1f, %.1f) classified as: %s%n", 
                testPoint.getX(), testPoint.getY(), predictedClass);

            //график для каждой тестовой точки
            String filename = String.format("classification_%.1f_%.1f.png", 
                testPoint.getX(), testPoint.getY());
            ChartGenerator.generateChart(classifier, classifiedPoint, filename);           
         
        }
    }
}
