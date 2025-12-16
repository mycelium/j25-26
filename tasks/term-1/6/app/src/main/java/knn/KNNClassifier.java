package knn;

import java.util.*;
import java.util.stream.Collectors;

public class KNNClassifier {
    private List<Point> trainingData;
    private int k;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingPoint(Point point) {
        trainingData.add(point);
    }

    public String classify(Point unknownPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("Нет тренировочных данных");
        }

        List<PointDistance> distances = trainingData.stream()
                .map(point -> new PointDistance(point, point.distanceTo(unknownPoint)))
                .collect(Collectors.toList());

        distances.sort(Comparator.comparingDouble(PointDistance::getDistance));

        List<PointDistance> nearestNeighbors = distances.stream()
                .limit(k)
                .collect(Collectors.toList());

        Map<String, Integer> voteCount = new HashMap<>();
        for (PointDistance neighbor : nearestNeighbors) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    private static class PointDistance {
        private Point point;
        private double distance;

        public PointDistance(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }

        public Point getPoint() { return point; }
        public double getDistance() { return distance; }
    }
}
