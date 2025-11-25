package org.example;
import java.util.*;
import java.util.stream.Collectors;

public class KNNClassifier {
    private List<Point> trainingData;
    private int k;
    
    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }
    
    public void addTrainingPoint(Point point) {
        trainingData.add(point);
    }
    
    public void addTrainingPoints(List<Point> points) {
        trainingData.addAll(points);
    }
    
    public String classify(Point unknownPoint) {
        if (trainingData.isEmpty()) {
            return "Unknown";
        }
        
        // Calculate distances to all training points
        List<PointDistance> distances = trainingData.stream()
            .map(point -> new PointDistance(point, point.distanceTo(unknownPoint)))
            .collect(Collectors.toList());
        
        // Sort by distance
        distances.sort(Comparator.comparingDouble(PointDistance::getDistance));
        
        // Take k nearest neighbors
        List<PointDistance> nearestNeighbors = distances.subList(0, 
            Math.min(k, distances.size()));
        
        // Count votes per class
        Map<String, Integer> voteCount = new HashMap<>();
        for (PointDistance neighbor : nearestNeighbors) {
            String label = neighbor.getPoint().getLabel();
            voteCount.put(label, voteCount.getOrDefault(label, 0) + 1);
        }
        
        // Return class with most votes
        return voteCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get()
            .getKey();
    }
    
    // Internal class for point + distance
    private static class PointDistance {
        private Point point;
        private double distance;
        
        public PointDistance(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }
        
        public Point getPoint() { return point; }
        public double getDistance() { return distance; }
    }
}