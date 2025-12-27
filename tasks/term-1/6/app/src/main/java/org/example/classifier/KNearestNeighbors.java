package org.example.classifier;

import java.util.*;

public class KNearestNeighbors {
    private List<DataPoint> trainingData;
    private int k;

    public KNearestNeighbors(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingPoint(DataPoint point) {
        trainingData.add(point);
    }

    public void addTrainingPoints(List<DataPoint> points) {
        trainingData.addAll(points);
    }

    public String classify(DataPoint unknownPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data available");
        }

        List<Neighbor> neighbors = new ArrayList<>();

        for (DataPoint trainingPoint : trainingData) {
            double distance = unknownPoint.distanceTo(trainingPoint);
            neighbors.add(new Neighbor(distance, trainingPoint.getLabel()));
        }

        neighbors.sort(Comparator.comparingDouble(Neighbor::getDistance));

        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String label = neighbors.get(i).getLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        String bestLabel = null;
        int maxCount = -1;

        for (Map.Entry<String, Integer> entry : labelCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestLabel = entry.getKey();
            }
        }

        return bestLabel;
    }

    private static class Neighbor {
        private double distance;
        private String label;

        public Neighbor(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }

        public double getDistance() {
            return distance;
        }

        public String getLabel() {
            return label;
        }
    }

    public List<DataPoint> getTrainingData() {
        return Collections.unmodifiableList(trainingData);
    }
}