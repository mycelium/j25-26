package org.example;

import java.util.*;

public class KNN {
    private int k;
    private List<Point> trainingData;
    
    public KNN(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }
    
    public KNN(List<Point> trainingData, int k) {
        this.trainingData = trainingData;
        this.k = k;
    }
    
    public void fit(List<Point> trainingData) {
        this.trainingData = new ArrayList<>(trainingData);
    }
    
    public String classify(Point point) {
        return predict(point);
    }
    
    public String predict(Point point) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("Модель не обучена. Сначала вызовите fit().");
        }
        
        List<DistanceLabel> distances = new ArrayList<>();
        for (Point trainingPoint : trainingData) {
            double distance = point.distanceTo(trainingPoint);
            distances.add(new DistanceLabel(distance, trainingPoint.getLabel()));
        }
        
        Collections.sort(distances);
        
        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k && i < distances.size(); i++) {
            String label = distances.get(i).label;
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
    
    public List<String> predict(List<Point> points) {
        List<String> predictions = new ArrayList<>();
        for (Point point : points) {
            predictions.add(predict(point));
        }
        return predictions;
    }
    
    private static class DistanceLabel implements Comparable<DistanceLabel> {
        double distance;
        String label;
        
        DistanceLabel(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }
        
        @Override
        public int compareTo(DistanceLabel other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    public int getK() { return k; }
    public List<Point> getTrainingData() { return trainingData; }
}
