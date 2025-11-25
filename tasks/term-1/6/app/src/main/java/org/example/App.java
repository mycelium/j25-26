package org.example;
import java.util.*;

public class App {
    public static void main(String[] args) {
        System.out.println("KNN Classification Visualizer ===");
        
        // Generate sample data with 4 classes
        List<Point> trainingData = generateTrainingData();
        List<Point> testPoints = generateTestPoints();
        
        // Display visualization 
        KNNVisualizer.displayVisualization(trainingData, testPoints, 3);
        
        System.out.println("Visualization displayed!");
        System.out.println("Training points: " + trainingData.size());
        System.out.println("Test points: " + testPoints.size());
    }
    
    private static List<Point> generateTrainingData() {
        List<Point> data = new ArrayList<>();
        Random random = new Random(42);
        
        // Class A - Top-left cluster
        for (int i = 0; i < 25; i++) {
            double x = 100 + random.nextGaussian() * 20;
            double y = 100 + random.nextGaussian() * 20;
            data.add(new Point(x, y, "A"));
        }
        
        // Class B - Bottom-right cluster  
        for (int i = 0; i < 25; i++) {
            double x = 600 + random.nextGaussian() * 25;
            double y = 450 + random.nextGaussian() * 25;
            data.add(new Point(x, y, "B"));
        }
        
        // Class C - Center cluster
        for (int i = 0; i < 20; i++) {
            double x = 400 + random.nextGaussian() * 30;
            double y = 300 + random.nextGaussian() * 30;
            data.add(new Point(x, y, "C"));
        }
        
        // Class D - Top-right cluster
        for (int i = 0; i < 20; i++) {
            double x = 600 + random.nextGaussian() * 25;
            double y = 100 + random.nextGaussian() * 25;
            data.add(new Point(x, y, "D"));
        }
        
        return data;
    }
    
    private static List<Point> generateTestPoints() {
        List<Point> testPoints = new ArrayList<>();
        
        // Test points in different regions
        testPoints.add(new Point(120, 110));  
        testPoints.add(new Point(580, 430));  
        testPoints.add(new Point(390, 290));    
        testPoints.add(new Point(590, 120));  
        testPoints.add(new Point(300, 200));  
        testPoints.add(new Point(500, 350));  
        testPoints.add(new Point(200, 400));  
        
        return testPoints;
    }
}