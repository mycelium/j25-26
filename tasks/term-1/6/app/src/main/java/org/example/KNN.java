package org.example;
import java.util.*;

public class KNN {
    private List<Point> trPoints;
    private int k;

    public KNN(List<Point> trPoints, int k){
        this.trPoints = trPoints;
        this.k = k;
    }

    public String classify(Point newPoint){
        List<Map.Entry<Point, Double>> distances = new ArrayList<>();
        for (Point p : trPoints) {
            double dist = newPoint.eDist(p);
            distances.add(Map.entry(p, dist));
        }
        distances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        Map<String, Integer> classCount = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String label = distances.get(i).getKey().getLabel();
            classCount.put(label, classCount.getOrDefault(label, 0) + 1);
        }

        return classCount.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

}
