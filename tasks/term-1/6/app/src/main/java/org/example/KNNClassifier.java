package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class KNNClassifier {

    public String classify(List<Point> trainingData, Point p, int k) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }
        if (k <= 0 || k > trainingData.size()) {
            throw new IllegalArgumentException("K must be positive and less than or equal to training data size");
        }

        List<Neighbor> neighbors = new ArrayList<>();
        for (Point trainingPoint : trainingData) {
            double distance = calculateDistance(p, trainingPoint);
            neighbors.add(new Neighbor(trainingPoint, distance));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::getDistance));

        List<Neighbor> kNearest = neighbors.subList(0, k);

        Map<String, Integer> voteCount = new HashMap<>();
        for (Neighbor neighbor : kNearest) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        String bestLabel = null;
        int maxVotes = -1;

        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                bestLabel = entry.getKey();
            }
        }

        return bestLabel;
    }

    private double calculateDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    private static class Neighbor {
        private final Point point;
        private final double distance;

        public Neighbor(Point point, double distance) {
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
}

