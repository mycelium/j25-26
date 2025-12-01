package org.example;

import java.util.*;

class Neighbor {
    private final Point point;
    private final double distance;

    public Neighbor(Point point, double distance) {
        this.point = point;
        this.distance = distance;
    }

    public Point getPoint() { return point; }
    public double getDistance() { return distance; }
}

public class KNNClassifier {
    private final List<Point> trainingData;
    private final int k;

    public KNNClassifier(List<Point> trainingData, int k) {
        this.trainingData = trainingData;
        this.k = k;
    }

    public String predict(Point newPoint) {
        List<Neighbor> distances = new ArrayList<>();

        for (Point p : trainingData) {
            double dist = calculateDistance(newPoint, p);
            distances.add(new Neighbor(p, dist));
        }

        distances.sort(Comparator.comparingDouble(Neighbor::getDistance));

        Map<String, Integer> classCounts = new HashMap<>();
        for (int i = 0; i < Math.min(k, distances.size()); i++) {
            String label = distances.get(i).getPoint().getLabel();
            classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
        }

        return classCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private double calculateDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) +
                Math.pow(a.getY() - b.getY(), 2));
    }
}
