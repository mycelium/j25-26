package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNNClassifier {
    private List<Point> trainingData;
    private int k;

    public KNNClassifier(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingPoint(Point point) {
        trainingData.add(point);
    }

    public void addTrainingPoints(List<Point> points) {
        trainingData.addAll(points);
    }

    public void train(List<Point> trainingData) {
        this.trainingData.addAll(trainingData);
    }

    public String classify(Point testPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data available");
        }

        List<Neighbor> neighbors = new ArrayList<>();
        
        for (Point trainPoint : trainingData) {
            double distance = testPoint.distanceTo(trainPoint);
            neighbors.add(new Neighbor(distance, trainPoint.getLabel()));
        }

        Collections.sort(neighbors);

        Map<String, Integer> voteCount = new HashMap<>();
        
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String label = neighbors.get(i).label;
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(voteCount.entrySet(), 
            Map.Entry.comparingByValue()).getKey();
    }

    private static class Neighbor implements Comparable<Neighbor> {
        double distance;
        String label;

        Neighbor(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }

        @Override
        public int compareTo(Neighbor other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public List<Point> getTrainingData() {
        return Collections.unmodifiableList(trainingData);
    }
    
    public int getK() {
        return k;
    }
}