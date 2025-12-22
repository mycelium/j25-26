package org.example;

import java.util.*;

public class KNNClassifier {

    private final List<LabeledPoint> trainPoints;
    private final int k;

    public KNNClassifier(List<LabeledPoint> trainPoints, int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (trainPoints == null || trainPoints.isEmpty())
            throw new IllegalArgumentException("training set is empty");
        this.trainPoints = new ArrayList<>(trainPoints);
        this.k = Math.min(k, trainPoints.size());
    }

    public String predict(double x, double y) {
        // сортируем точки по расстоянию до (x,y)
        List<LabeledPoint> sorted = new ArrayList<>(trainPoints);
        sorted.sort(Comparator.comparingDouble(p -> p.distanceTo(x, y)));

        // считаем частоты меток среди k ближайших
        Map<String, Integer> votes = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String lbl = sorted.get(i).label;
            votes.put(lbl, votes.getOrDefault(lbl, 0) + 1);
        }

        // выбираем метку с максимумом голосов
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }
}
