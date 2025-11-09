package org.example;
import java.util.*;

public class KNNClassifier {
    private List<Point> trainingData;
    private int k;

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
            throw new IllegalStateException("No training data available");
        }
        List<DistancePoint> distances = new ArrayList<>(); //расстояние до всех точек
        for (Point trainingPoint : trainingData) {
            double distance = unknownPoint.distanceTo(trainingPoint);
            distances.add(new DistancePoint(distance, trainingPoint));
        }
        distances.sort(Comparator.comparingDouble(DistancePoint::getDistance)); //сортировка
        Map<String, Integer> labelCount = new HashMap<>(); //берем ближайших соседей
        for (int i = 0; i < Math.min(k, distances.size()); i++) {
            String label = distances.get(i).getPoint().getLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }
        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey(); //наиболее частая метка
    }

    private static class DistancePoint {
        private double distance;
        private Point point;

        public DistancePoint(double distance, Point point) {
            this.distance = distance;
            this.point = point;
        }

        public double getDistance() { return distance; }
        public Point getPoint() { return point; }
    }
}