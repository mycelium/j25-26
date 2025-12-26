package org.example;

import java.util.*;

public class KNNClassifier {

    private int neighborsCount;
    private DistanceMode mode;
    private DataSet training;

    private static final double EPS = 1e-9;

    public KNNClassifier(int neighborsCount, DistanceMode mode) {
        if (neighborsCount <= 0) {
            throw new IllegalArgumentException("neighborsCount must be > 0");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        this.neighborsCount = neighborsCount;
        this.mode = mode;
        this.training = new DataSet();
    }

    public void fit(DataSet data) {
        if (data == null || data.size() == 0) {
            throw new IllegalArgumentException("Training data is empty");
        }
        this.training = data;
    }

    public void fit(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Training data is empty");
        }
        this.training = new DataSet(points);
    }

    public String classify(Point query) {
        if (training == null || training.size() == 0) {
            throw new IllegalStateException("Classifier is not fitted");
        }
        if (query == null) {
            throw new IllegalArgumentException("Query point is null");
        }

        List<Point> ordered = sortByDistance(query);
        List<Point> neighbors = takeTopK(ordered);

        return (mode == DistanceMode.UNIFORM)
                ? majorityLabel(neighbors)
                : weightedMajorityLabel(neighbors);
    }

    public List<Point> neighbors(Point query) {
        List<Point> ordered = sortByDistance(query);
        return takeTopK(ordered);
    }

    public List<String> classifyBatch(List<Point> queries) {
        List<String> result = new ArrayList<>();
        for (Point q : queries) {
            result.add(classify(q));
        }
        return result;
    }

    private List<Point> sortByDistance(Point query) {
        List<Point> copy = training.getPoints();
        for (Point p : copy) {
            double d = distance(p, query);
            p.setDistanceToQuery(d);
        }
        copy.sort(Comparator.comparingDouble(Point::getDistanceToQuery));
        return copy;
    }

    private List<Point> takeTopK(List<Point> ordered) {
        int limit = Math.min(neighborsCount, ordered.size());
        return new ArrayList<>(ordered.subList(0, limit));
    }

    private String majorityLabel(List<Point> neighbors) {
        if (neighbors.isEmpty()) {
            return null;
        }
        Map<String, Integer> counter = new HashMap<>();
        for (Point p : neighbors) {
            String lbl = p.getLabel();
            counter.put(lbl, counter.getOrDefault(lbl, 0) + 1);
        }
        return argMax(counter);
    }

    private String weightedMajorityLabel(List<Point> neighbors) {
        if (neighbors.isEmpty()) {
            return null;
        }
        Map<String, Double> weights = new HashMap<>();
        for (Point p : neighbors) {
            String lbl = p.getLabel();
            double w = 1.0 / (p.getDistanceToQuery() + EPS);
            weights.put(lbl, weights.getOrDefault(lbl, 0.0) + w);
        }
        return argMaxDouble(weights);
    }

    private double distance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private String argMax(Map<String, Integer> counts) {
        String best = null;
        int bestVal = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestVal) {
                bestVal = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    private String argMaxDouble(Map<String, Double> counts) {
        String best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> e : counts.entrySet()) {
            if (e.getValue() > bestVal) {
                bestVal = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    public int getNeighborsCount() {
        return neighborsCount;
    }

    public void setNeighborsCount(int neighborsCount) {
        if (neighborsCount <= 0) {
            throw new IllegalArgumentException("neighborsCount must be > 0");
        }
        this.neighborsCount = neighborsCount;
    }

    public DistanceMode getMode() {
        return mode;
    }

    public void setMode(DistanceMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        this.mode = mode;
    }

    public DataSet getTraining() {
        return training;
    }
}