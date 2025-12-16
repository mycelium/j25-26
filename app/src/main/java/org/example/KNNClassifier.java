package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K-Nearest Neighbors classifier implementation
 */
public class KNNClassifier {
    private final List<Point> trainingData;
    private final int k;

    public KNNClassifier(List<Point> trainingData, int k) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("K must be positive");
        }
        if (k > trainingData.size()) {
            throw new IllegalArgumentException("K cannot be larger than training data size");
        }

        this.trainingData = new ArrayList<>(trainingData);
        this.k = k;
    }

    /**
     * Classifies a new point based on k-nearest neighbors
     */
    public String classify(Point testPoint) {
        if (testPoint == null) {
            throw new IllegalArgumentException("Test point cannot be null");
        }

        // Calculate distances to all training points
        List<NeighborDistance> distances = new ArrayList<>();
        for (Point trainPoint : trainingData) {
            double distance = testPoint.distanceTo(trainPoint);
            distances.add(new NeighborDistance(trainPoint, distance));
        }

        // Sort by distance (ascending)
        distances.sort(Comparator.comparingDouble(NeighborDistance::getDistance));

        // Get k nearest neighbors
        List<NeighborDistance> kNearest = distances.subList(0, k);

        // Count votes for each class
        Map<String, Integer> voteCount = new HashMap<>();
        for (NeighborDistance neighbor : kNearest) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        // Find the class with maximum votes
        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Helper class to store point and its distance
     */
    private static class NeighborDistance {
        private final Point point;
        private final double distance;

        public NeighborDistance(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }

        public Point getPoint() {
            return point;
        }

        public double getDistance() {
            return distance;
        }
    }

    public int getK() {
        return k;
    }

    public int getTrainingDataSize() {
        return trainingData.size();
    }
}
