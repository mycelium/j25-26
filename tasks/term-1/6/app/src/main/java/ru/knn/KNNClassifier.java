package ru.knn;

import java.util.*;

public class KNNClassifier {
    private final List<Point> trainingData;
    private final int k;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void addTrainingPoints(List<Point> points) {
        trainingData.addAll(points);
    }

    public String classify(Point unknownPoint) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("Нет обучающих данных");
        }

        List<Point> sortedPoints = trainingData.stream()
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(unknownPoint)))
                .toList();

        List<Point> nearestNeighbors = sortedPoints.subList(0, Math.min(k, sortedPoints.size()));

        Map<String, Integer> classVotes = new HashMap<>();
        for (Point neighbor : nearestNeighbors) {
            String label = neighbor.label();
            classVotes.put(label, classVotes.getOrDefault(label, 0) + 1);
        }

        return Collections.max(classVotes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}