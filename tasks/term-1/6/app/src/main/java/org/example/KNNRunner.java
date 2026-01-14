package org.example;

import java.util.*;

public class KNNRunner {

    public static void main(String[] args) {
        System.out.println("K-Nearest Neighbors Classifier \n");

        KNNClassifier classifier = new KNNClassifier(3);
        KNNVisualizer visualizer = new KNNVisualizer();

        List<Point> trainingData = generateSampleData();
        classifier.addTrainingData(trainingData);

        System.out.println("Training data generated with " + trainingData.size() + " points");
        System.out.println("Classes: " + classifier.getClasses() + "\n");

        visualizer.visualize(classifier, null);

        testExampleClassifications(classifier, visualizer);

        runInteractiveMode(classifier, visualizer);

        System.out.println("Program completed successfully!");
    }

    private static List<Point> generateSampleData() {
        List<Point> data = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < 12; i++) {
            double x = 3 + random.nextGaussian() * 0.7;
            double y = 3 + random.nextGaussian() * 0.7;
            data.add(new Point(x, y, "Class-X"));
        }

        for (int i = 0; i < 12; i++) {
            double x = 8 + random.nextGaussian() * 0.5;
            double y = 5 + random.nextGaussian() * 0.5;
            data.add(new Point(x, y, "Class-Y"));
        }

        for (int i = 0; i < 12; i++) {
            double x = 5 + random.nextGaussian() * 0.6;
            double y = 9 + random.nextGaussian() * 0.6;
            data.add(new Point(x, y, "Class-Z"));
        }

        return data;
    }

    private static void testExampleClassifications(KNNClassifier classifier, KNNVisualizer visualizer) {
        System.out.println(" Example Classifications ");

        Point[] testPoints = {
                new Point(3.5, 3.5),
                new Point(8.2, 5.3),
                new Point(4.5, 9.1),
                new Point(6.2, 6.0)
        };

        for (Point testPoint : testPoints) {
            String predictedClass = classifier.classify(testPoint);
            System.out.printf("Point (%.1f, %.1f) -> %s%n",
                    testPoint.getX(), testPoint.getY(), predictedClass);

            visualizer.visualize(classifier, testPoint);
        }
    }

    private static void runInteractiveMode(KNNClassifier classifier, KNNVisualizer visualizer) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Interactive Mode");
            System.out.println("Enter point coordinates to classify (format: x y)");
            System.out.println("Type 'quit' to exit\n");

            while (true) {
                System.out.print("Enter coordinates: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit")) {
                    break;
                }

                try {
                    String[] parts = input.split("\\s+");
                    if (parts.length != 2) {
                        System.out.println("Please enter exactly two numbers separated by space");
                        continue;
                    }

                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);

                    Point unknownPoint = new Point(x, y);
                    String predictedClass = classifier.classify(unknownPoint);

                    System.out.printf(">>> Point (%.2f, %.2f) classified as: %s%n",
                            x, y, predictedClass);

                    visualizer.visualize(classifier, unknownPoint);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter numbers only.\n");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage() + "\n");
                }
            }

            System.out.println("Goodbye!");
            scanner.close();
        } catch (Exception e) {
            System.out.println("Interactive mode not available in this environment");
        }
    }
}
