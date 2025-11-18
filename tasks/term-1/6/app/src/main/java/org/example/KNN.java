package org.example;

import java.util.*;

class KNN {
    private int k;
    private List<Point> dataset;

    public KNN(int k) {
        this.k = k;
        this.dataset = new ArrayList<>(); 
    }

    public void setDataset(List<Point> newDataset) {
        this.dataset = new ArrayList<>(newDataset);
    }

    public void addPoint(Point point) {
        dataset.add(point);
    }

    public String predict(Point point) {
        if (dataset.isEmpty()) {
            throw new IllegalStateException("Dataset is empty now! There should be points in it!");
        }

        List<CompareDist> sortedDists = new ArrayList<>();

        for (Point datasetPoint : dataset) {
            double dist = datasetPoint.dist(point);
            sortedDists.add(new CompareDist(dist, datasetPoint.getLabel()));
        }

        Collections.sort(sortedDists);

        Map<String, Integer> frequency = new HashMap<>();
        for (int i = 0; i < Math.min(k, sortedDists.size()); i++) {
            String label = sortedDists.get(i).label;
            frequency.put(label, frequency.getOrDefault(label, 0) + 1);
        }

        return frequency.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }


    private static class CompareDist implements Comparable<CompareDist> {
        private double dist;
        private String label;

        public CompareDist(double dist, String label) {
            this.dist = dist;
            this.label = label;
        }
    
        @Override
        public int compareTo(CompareDist point) {
            return Double.compare(this.dist, point.dist);
        }

    }
}