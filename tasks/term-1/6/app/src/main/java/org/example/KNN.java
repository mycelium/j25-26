package org.example;

import java.util.*;

public class KNN {
    private int k;
    private List<Point> trainingData;
    
    public KNN(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }
    
    // Метод fit - обучение модели. Для данного класса - это просто сохранение данных.
    public void fit(List<Point> trainingData) {
        this.trainingData = new ArrayList<>(trainingData);
    }
    
    // Метод predict - предсказание для одной точки.
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

        // Наиболее частый класс среди соседей.
        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
    
    public List<String> predict(List<Point> points) {
        List<String> predictions = new ArrayList<>();
        for (Point point : points) {
            predictions.add(predict(point));
        }
        return predictions;
    }
    
    // Класс для хранения расстояния и метки.
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