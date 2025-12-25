package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KnnClassifier {

    private List<Point> trainPoints;
    private int k;

    public KnnClassifier(List<Point> trainPoints, int k) {
        this.trainPoints = trainPoints;
        this.k = k;
    }

    public int classify(double x, double y) {
        List<Neighbor> neighbors = new ArrayList<>();

        for (Point p : trainPoints) {
            double dx = p.x - x;
            double dy = p.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            neighbors.add(new Neighbor(dist, p.clazz));
        }

        Collections.sort(neighbors, new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor n1, Neighbor n2) {
                return Double.compare(n1.distance, n2.distance);
            }
        });

        int maxClass = getMaxClassLabel();
        int[] votes = new int[maxClass + 1];

        for (int i = 0; i < k && i < neighbors.size(); i++) {
            Neighbor n = neighbors.get(i);
            if (n.clazz >= 0 && n.clazz < votes.length) {
                votes[n.clazz]++;
            }
        }

        int bestClass = 0;
        int bestVotes = -1;
        for (int c = 0; c < votes.length; c++) {
            if (votes[c] > bestVotes) {
                bestVotes = votes[c];
                bestClass = c;
            }
        }

        return bestClass;
    }

    private int getMaxClassLabel() {
        int max = 0;
        for (Point p : trainPoints) {
            if (p.clazz > max) {
                max = p.clazz;
            }
        }
        return max;
    }

    private static class Neighbor {
        public double distance;
        public int clazz;

        public Neighbor(double distance, int clazz) {
            this.distance = distance;
            this.clazz = clazz;
        }
    }
}
