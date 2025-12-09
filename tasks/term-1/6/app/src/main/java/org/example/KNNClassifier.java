package org.example;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

public class KNNClassifier {
    private int k;
    private DataSet trainingData;
    
    public KNNClassifier(int k) {
        this.k = k;
        this.trainingData = new DataSet();
    }
    
    public KNNClassifier(int k, DataSet trainingData) {
        this.k = k;
        this.trainingData = trainingData;
    }
    
    public void train(DataSet trainingData) {
        this.trainingData = trainingData;
    }
    
    public void train(List<Point> trainingPoints) {
        this.trainingData = new DataSet(trainingPoints);
    }
    
    public String predict(Point newPoint) {
        if (trainingData == null || trainingData.size() == 0) {
            throw new IllegalStateException("Classifier has not been trained. Call train() first.");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive. Current k = " + k);
        }
        
        List<Point> trainingPoints = trainingData.getPoints();
        
        
        for (Point point : trainingPoints) {
            double distance = calculateEuclideanDistance(point, newPoint);
            point.setDistanceToTarget(distance);
        }
        
       
        Collections.sort(trainingPoints, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.getDistanceToTarget(), p2.getDistanceToTarget());
            }
        });
        
        int actualK = Math.min(k, trainingPoints.size());
        List<Point> nearestNeighbors = new ArrayList<>();
        for (int i = 0; i < actualK; i++) {
            nearestNeighbors.add(trainingPoints.get(i));
        }
        
        
        Map<String, Integer> votes = new HashMap<>();
        for (Point neighbor : nearestNeighbors) {
            String label = neighbor.getLabel();
            votes.put(label, votes.getOrDefault(label, 0) + 1);
        }
       
        String predictedClass = null;
        int maxVotes = 0;
        
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                predictedClass = entry.getKey();
            }
        }
        
        if (predictedClass == null) {
            predictedClass = nearestNeighbors.get(0).getLabel();
        }
        
        return predictedClass;
    }
    
    public List<Point> getNearestNeighbors(Point newPoint) {
        List<Point> trainingPoints = trainingData.getPoints();
        
        for (Point point : trainingPoints) {
            double distance = calculateEuclideanDistance(point, newPoint);
            point.setDistanceToTarget(distance);
        }
        
        Collections.sort(trainingPoints, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.getDistanceToTarget(), p2.getDistanceToTarget());
            }
        });
        
        int actualK = Math.min(k, trainingPoints.size());
        List<Point> nearestNeighbors = new ArrayList<>();
        for (int i = 0; i < actualK; i++) {
            nearestNeighbors.add(trainingPoints.get(i));
        }
        
        return nearestNeighbors;
    }
    
    public List<String> predictBatch(List<Point> testPoints) {
        List<String> predictions = new ArrayList<>();
        for (Point testPoint : testPoints) {
            predictions.add(predict(testPoint));
        }
        return predictions;
    }
    
    private double calculateEuclideanDistance(Point p1, Point p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public int getK() {
        return k;
    }
    
    public void setK(int k) {
        this.k = k;
    }
    
    public DataSet getTrainingData() {
        return trainingData;
    }
}