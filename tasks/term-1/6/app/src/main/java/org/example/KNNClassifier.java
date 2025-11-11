package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class KNNClassifier {
    private final List<Point> trainingData;
    private final int k;

    public KNNClassifier(int k) {
        this.trainingData = new ArrayList<>();
        this.k = k;
    }

    public void addTrainingPoint(Point point) {
        trainingData.add(point);
    }

    public void addTrainingPoints(List<Point> points) {
        trainingData.addAll(points);
    }

    public String classify(Point unknownPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("no training data");
        }

        //расстояния до всех точек
        List<PointDistance> distances = trainingData.stream()
                .map(point -> new PointDistance(point, point.distanceTo(unknownPoint)))
                .sorted(Comparator.comparingDouble(PointDistance::getDistance))
                .collect(Collectors.toList());

        //выбирается k ближайших соседей
        List<PointDistance> nearestNeighbors = distances.stream()
                .limit(k)
                .collect(Collectors.toList());

        //распределение по классам

        Map<String, Integer> voteCount = new HashMap<>();
        for (PointDistance neighbor : nearestNeighbors) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }


        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("Classification failed"));
    }

    public List<Point> getTrainingData() {
        return Collections.unmodifiableList(trainingData);
    }

    private static class PointDistance {
        private final Point point;
        private final double distance;

        public PointDistance(Point point, double distance) {
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
