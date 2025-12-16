package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates sample training data with multiple classes
 */
public class DataGenerator {
    private final Random random = new Random(42); // Fixed seed for reproducibility

    /**
     * Generates sample data with 3 classes arranged in clusters
     */
    public List<Point> generateSampleData(int pointsPerClass) {
        List<Point> data = new ArrayList<>();

        // Class A: centered at (2, 2)
        data.addAll(generateCluster("A", 2.0, 2.0, pointsPerClass, 0.8));

        // Class B: centered at (8, 3)
        data.addAll(generateCluster("B", 8.0, 3.0, pointsPerClass, 0.8));

        // Class C: centered at (5, 8)
        data.addAll(generateCluster("C", 5.0, 8.0, pointsPerClass, 0.8));

        return data;
    }

    /**
     * Generates a cluster of points around a center
     */
    private List<Point> generateCluster(String label, double centerX, double centerY,
                                      int count, double spread) {
        List<Point> cluster = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Generate random point within spread distance from center
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * spread;

            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            cluster.add(new Point(x, y, label));
        }

        return cluster;
    }

    /**
     * Generates test points for classification
     */
    public List<Point> generateTestPoints(int count) {
        List<Point> testPoints = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * 12; // 0-12 range
            double y = random.nextDouble() * 12; // 0-12 range
            testPoints.add(new Point(x, y));
        }

        return testPoints;
    }

    /**
     * Gets the class colors for visualization
     */
    public static java.awt.Color getClassColor(String label) {
        switch (label) {
            case "A": return java.awt.Color.RED;
            case "B": return java.awt.Color.BLUE;
            case "C": return java.awt.Color.GREEN;
            default: return java.awt.Color.BLACK;
        }
    }
}
