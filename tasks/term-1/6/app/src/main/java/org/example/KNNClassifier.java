package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация классификатора K-ближайших соседей
 */
public class KNNClassifier {
    private final List<Point> trainingData;
    private final int k;

    public KNNClassifier(List<Point> trainingData, int k) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("K must be positive");
        }
        if (k > trainingData.size()) {
            throw new IllegalArgumentException("K cannot be larger than training data size");
        }

        this.trainingData = new ArrayList<>(trainingData);
        this.k = k;
    }

    /**
     * Классифицирует новую точку на основе k-ближайших соседей
     */
    public String classify(Point testPoint) {
        if (testPoint == null) {
            throw new IllegalArgumentException("Test point cannot be null");
        }

        // Вычисление расстояний до всех обучающих точек
        List<NeighborDistance> distances = new ArrayList<>();
        for (Point trainPoint : trainingData) {
            double distance = testPoint.distanceTo(trainPoint);
            distances.add(new NeighborDistance(trainPoint, distance));
        }

        // Сортировка по расстоянию (по возрастанию)
        distances.sort(Comparator.comparingDouble(NeighborDistance::getDistance));

        // Получение k ближайших соседей
        List<NeighborDistance> kNearest = distances.subList(0, k);

        // Подсчёт голосов для каждого класса
        Map<String, Integer> voteCount = new HashMap<>();
        for (NeighborDistance neighbor : kNearest) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        // Определение класса с максимальным числом голосов
        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    /**
     * Вспомогательный класс для хранения точки и расстояния до неё
     */
    private static class NeighborDistance {
        private final Point point;
        private final double distance;

        public NeighborDistance(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }

        public Point getPoint() {
            return point;
        }

        public double getDistance() {
            return distance;
        }
    }

    public int getK() {
        return k;
    }

    public int getTrainingDataSize() {
        return trainingData.size();
    }
}
