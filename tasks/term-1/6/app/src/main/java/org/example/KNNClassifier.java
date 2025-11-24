package org.example;

import java.util.*;

public class KNNClassifier {
    private List<Point> trainingData;
    private int k;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingData(List<Point> points) {
        trainingData.addAll(points);
    }

    public void addTrainingData(Point point) {
        trainingData.add(point);
    }

    public String classify(Point testPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data available.");
        }

        List<DistanceLabel> distanceLabels = new ArrayList<>();
        for (Point trainPoint : trainingData) {
            double distance = testPoint.distanceTo(trainPoint);
            distanceLabels.add(new DistanceLabel(distance, trainPoint.getLabel()));
        }

        distanceLabels.sort(Comparator.comparingDouble(DistanceLabel::getDistance));
        List<DistanceLabel> nearestNeighbors = distanceLabels.subList(0, Math.min(k, distanceLabels.size()));

        Map<String, Integer> voteCount = new HashMap<>();
        for (DistanceLabel neighbor : nearestNeighbors) {
            voteCount.put(neighbor.getLabel(), voteCount.getOrDefault(neighbor.getLabel(), 0) + 1);
        }

        return Collections.max(voteCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static class DistanceLabel {
        private double distance;
        private String label;

        public DistanceLabel(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }

        public double getDistance() { return distance; }
        public String getLabel() { return label; }
    }

    public List<Point> getTrainingData() { return trainingData; }
    public int getK() { return k; }
}
