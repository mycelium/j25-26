package org.example;

import java.util.ArrayList;
import java.util.List;

public class DataSet {
    private List<Point> points;
    
    public DataSet() {
        this.points = new ArrayList<>();
    }
    
    public DataSet(List<Point> points) {
        this.points = new ArrayList<>(points);
    }
    
    public void addPoint(Point point) {
        points.add(point);
    }
    
    public List<Point> getPoints() {
        return new ArrayList<>(points); 
    }
    
    public void setPoints(List<Point> points) {
        this.points = new ArrayList<>(points);
    }
    
    public int size() {
        return points.size();
    }
    
    public List<String> getUniqueLabels() {
        List<String> labels = new ArrayList<>();
        for (Point point : points) {
            String label = point.getLabel();
            if (label != null && !label.isEmpty() && !labels.contains(label)) {
                labels.add(label);
            }
        }
        return labels;
    }
    
 
    public static DataSet getFixedDataSet1() {
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < 15; i++) {
            double x = 0.2 + i * 0.03;
            double y = 0.2 + i * 0.02;
            points.add(new Point(x, y, "Class A"));
        }
        
        points.add(new Point(0.15, 0.25, "Class A"));
        points.add(new Point(0.25, 0.15, "Class A"));
        points.add(new Point(0.3, 0.2, "Class A"));
        points.add(new Point(0.2, 0.3, "Class A"));
        
        for (int i = 0; i < 15; i++) {
            double x = 0.6 + i * 0.03;
            double y = 0.6 + i * 0.02;
            points.add(new Point(x, y, "Class B"));
        }
        
        points.add(new Point(0.65, 0.7, "Class B"));
        points.add(new Point(0.7, 0.65, "Class B"));
        points.add(new Point(0.75, 0.6, "Class B"));
        points.add(new Point(0.6, 0.75, "Class B"));
        
        for (int i = 0; i < 15; i++) {
            double x = 0.4 + i * 0.02;
            double y = 0.8 - i * 0.02;
            points.add(new Point(x, y, "Class C"));
        }
        
        points.add(new Point(0.45, 0.45, "Class C"));
        points.add(new Point(0.5, 0.4, "Class C"));
        points.add(new Point(0.4, 0.5, "Class C"));
        points.add(new Point(0.55, 0.35, "Class C"));
        
        return new DataSet(points);
    }
    
    public static DataSet getFixedDataSet2() {
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < 12; i++) {
            double x = 0.3 + i * 0.04;
            double y = 0.3 + i * 0.03;
            points.add(new Point(x, y, "Class X"));
        }
        
        for (int i = 0; i < 12; i++) {
            double x = 0.4 + i * 0.04;
            double y = 0.2 + i * 0.04;
            points.add(new Point(x, y, "Class Y"));
        }
        
        for (int i = 0; i < 12; i++) {
            double x = 0.7 + (i % 4) * 0.05;
            double y = 0.7 + (i / 4) * 0.05;
            points.add(new Point(x, y, "Class Z"));
        }
        
        points.add(new Point(0.8, 0.2, "Class Z"));
        points.add(new Point(0.2, 0.8, "Class X"));
        
        return new DataSet(points);
    }
    
    public static DataSet getFixedDataSet3() {
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double x = 0.1 + i * 0.04;
                double y = 0.1 + j * 0.04;
                points.add(new Point(x, y, "Class A"));
            }
        }
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double x = 0.6 + i * 0.04;
                double y = 0.1 + j * 0.04;
                points.add(new Point(x, y, "Class B"));
            }
        }
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double x = 0.1 + i * 0.04;
                double y = 0.6 + j * 0.04;
                points.add(new Point(x, y, "Class G"));
            }
        }
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double x = 0.6 + i * 0.04;
                double y = 0.6 + j * 0.04;
                points.add(new Point(x, y, "Class D"));
            }
        }
        
        points.add(new Point(0.5, 0.5, "Class A"));
        points.add(new Point(0.5, 0.5, "Class B"));
        points.add(new Point(0.5, 0.5, "Class G"));
        points.add(new Point(0.5, 0.5, "Class D"));
        
        return new DataSet(points);
    }
    
    public static List<Point> generateRandomTestPoints(int count) {
        List<Point> testPoints = new ArrayList<>();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < count; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            testPoints.add(new Point(x, y, null));
        }
        
        return testPoints;
    }
}