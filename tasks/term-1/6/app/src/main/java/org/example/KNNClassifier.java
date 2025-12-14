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

    public String classify(Point testPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data");
        }

        List<DistanceLabel> distances = new ArrayList<>();
        for (Point trainPoint : trainingData) {
            double distance = testPoint.distanceTo(trainPoint);
            distances.add(new DistanceLabel(distance, trainPoint.getLabel()));
        }

        distances.sort(Comparator.comparingDouble(DistanceLabel::getDistance));

        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < Math.min(k, distances.size()); i++) {
            String label = distances.get(i).getLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    //вспомогательный класс для хранения расстояния и метки
    private static class DistanceLabel {
        private double distance;
        private String label;

        public DistanceLabel(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }

        public double getDistance() { return distance; }
        public String getLabel() { return label; }
    }

    public List<Point> getTrainingData() {
        return trainingData;
    }
}