package org.example;

import java.util.*;
import java.util.stream.Collectors;

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

    public void addTrainingPoint(Point point) {
        trainingData.add(point);
    }

    public String classify(Point unknownPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data available");
        }

        // cортируем точки по расстоянию до неизвестной точки
        List<Point> sortedByDistance = trainingData.stream()
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(unknownPoint)))
                .collect(Collectors.toList());

        // берем k ближайших соседей
        List<Point> nearestNeighbors = sortedByDistance.subList(0, Math.min(k, sortedByDistance.size()));

        // подсчитываем голоса за каждый класс
        Map<String, Integer> voteCount = new HashMap<>();
        for (Point neighbor : nearestNeighbors) {
            String label = neighbor.getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        // находим класс с наибольшим количеством голосов
        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    // получение всех уникальных классов
    public Set<String> getClasses() {
        return trainingData.stream()
                .map(Point::getLabel)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public List<Point> getTrainingData() {
        return Collections.unmodifiableList(trainingData);
    }
}