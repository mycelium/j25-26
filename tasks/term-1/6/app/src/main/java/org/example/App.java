// src/main/java/org/example/App.java
package org.example;

import java.util.*;

public class App {

    public static class Point {
        private final double x, y;
        private final String label;

        public Point(double x, double y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String getLabel() {
            return label;
        }

        public double distanceSquared(Point other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return dx * dx + dy * dy;
        }
    }

    public static class KNNClassifier {
        private final List<Point> trainingData;
        private final int k;

        public KNNClassifier(List<Point> trainingData, int k) {
            if (trainingData == null || trainingData.isEmpty()) {
                throw new IllegalArgumentException("Empty data");
            }
            if (k <= 0) {
                throw new IllegalArgumentException("k must be positive");
            }
            this.trainingData = new ArrayList<>(trainingData);
            this.k = k;
        }

        public String classify(Point point) {
            if (point == null) {
                throw new IllegalArgumentException("Point cann't be null");
            }

            List<Point> neighbors = new ArrayList<>(trainingData);
            neighbors.sort(Comparator.comparingDouble(p -> p.distanceSquared(point)));

            Map<String, Integer> votes = new HashMap<>();
            for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
                String label = neighbors.get(i).getLabel();
                votes.merge(label, 1, Integer::sum);
            }

            return Collections.max(votes.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
    }

    public static void main(String[] args) {
        List<Point> train = Arrays.asList(
                new Point(0.5, 1.0, "A"),
                new Point(1.2, 0.8, "A"),
                new Point(0.9, 1.5, "A"),
                new Point(1.6, 1.3, "A"),
                new Point(0.7, 2.0, "A"),
                new Point(1.8, 0.9, "A"),
                new Point(1.1, 1.7, "A"),

                new Point(4.0, 7.0, "B"),
                new Point(4.8, 6.5, "B"),
                new Point(5.2, 7.3, "B"),
                new Point(4.5, 8.0, "B"),
                new Point(5.0, 6.8, "B"),
                new Point(4.2, 7.5, "B"),
                new Point(4.9, 7.2, "B"),

                new Point(8.5, 9.0, "C"),
                new Point(9.2, 8.7, "C"),
                new Point(8.8, 9.5, "C"),
                new Point(9.6, 9.3, "C"),
                new Point(8.3, 8.9, "C"),
                new Point(9.0, 8.5, "C"),
                new Point(8.7, 9.1, "C"));

        double[][] testPoints = {
                { 1.0, 1.2 },
                { 9.0, 9.0 },
                { 4.6, 7.1 },
                { 2.5, 3.0 },
                { 7.0, 8.0 },
                { 5.5, 4.0 }
        };

        KNNClassifier knn = new KNNClassifier(train, 3);

        for (int i = 0; i < testPoints.length; i++) {
            double x = testPoints[i][0];
            double y = testPoints[i][1];
            Point query = new Point(x, y, null);
            String pred = knn.classify(query);

            System.out.printf("Point %d: (%.1f, %.1f) → %s%n", i + 1, x, y, pred);
            Plotter.plot(train, query, pred, "plot_" + (i + 1) + ".png");
        }

        System.out.println("\nPlots are saved");
    }

    public String getGreeting() {
        return "KNN Classifier Ready!";
    }
}