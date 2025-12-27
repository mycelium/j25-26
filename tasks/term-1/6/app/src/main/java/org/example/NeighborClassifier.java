package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class NeighborClassifier {
    private final List<Point> referenceSet;
    private final int neighborCount;

    public NeighborClassifier(int k) {
        this.neighborCount = k;
        this.referenceSet = new ArrayList<>();
    }

    public void addReference(Point p) {
        referenceSet.add(p);
    }

    public String predict(Point query) {
        if (referenceSet.isEmpty()) {
            throw new IllegalStateException("Нет обучающих данных!");
        }

        List<DistanceRecord> records = referenceSet.stream()
                .map(p -> new DistanceRecord(p, p.distanceTo(query)))
                .sorted(Comparator.comparingDouble(DistanceRecord::getDist))
                .limit(neighborCount)
                .collect(Collectors.toList());

        Map<String, Long> votes = records.stream()
                .map(r -> r.getRefPoint().getLabel())
                .collect(Collectors.groupingBy(label -> label, Collectors.counting()));

        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private static class DistanceRecord {
        private final Point refPoint;
        private final double dist;

        public DistanceRecord(Point point, double d) {
            this.refPoint = point;
            this.dist = d;
        }

        public Point getRefPoint() { return refPoint; }
        public double getDist() { return dist; }
    }
}