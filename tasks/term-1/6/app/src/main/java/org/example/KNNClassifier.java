package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNNClassifier {
    private int k;
    private List<Point> trainingData;

    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void train(List<Point> data) {
        this.trainingData = new ArrayList<>(data);
    }

    public String classify(Point point) {
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data.");
        }

        List<Neighbor> neighbors = new ArrayList<>();
        for (Point p : trainingData) {
            double dist = point.distanceTo(p);
            neighbors.add(new Neighbor(dist, p.getLabel()));
        }

        Collections.sort(neighbors);

        Map<String, Integer> voteCount = new HashMap<>();
        for (int i = 0; i < k && i < neighbors.size(); i++) {
            String label = neighbors.get(i).label;
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }

        String bestLabel = null;
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                bestLabel = entry.getKey();
            }
        }
        return bestLabel;
    }

    public List<String> classifyMultiple(List<Point> points) {
        List<String> results = new ArrayList<>();
        for (Point p : points) {
            results.add(classify(p));
        }
        return results;
    }

    private static class Neighbor implements Comparable<Neighbor> {
        double distance;
        String label;

        Neighbor(double d, String l) {
            distance = d;
            label = l;
        }

        public int compareTo(Neighbor other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}