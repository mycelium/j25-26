package org.example;

import java.util.*;

public class KNNmethod {

    private List<Point> testDataPoints;
    private int k;


    public KNNmethod(int k) {
        this.k = k;
        this.testDataPoints = new ArrayList<>();
    }

    public void DataForMethod(List<Point> points) {
        testDataPoints.addAll(points);
    }

    public int classify(Point newPoint) {
        if (testDataPoints.isEmpty()) {
            throw new IllegalStateException("Нету данных");
        }

        List<Map.Entry<Double, Integer>> dis = new ArrayList<>();
        for (Point trainPoint : testDataPoints) {
            double distance = newPoint.EuclidDist(trainPoint);
            dis.add(new AbstractMap.SimpleEntry<>(distance, trainPoint.getClassLabel()));
        }
        
        dis.sort(Comparator.comparingDouble(Map.Entry::getKey));

        Map<Integer, Integer> labelCounts = new HashMap<>();
        for (int i = 0; i < Math.min(k, dis.size()); i++) {
            int label = dis.get(i).getValue();
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCounts.entrySet(),
                Map.Entry.comparingByValue()).getKey();
    }
}
