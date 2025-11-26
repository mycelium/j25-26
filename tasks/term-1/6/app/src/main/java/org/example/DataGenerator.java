package org.example;

import java.util.*;

public class DataGenerator {
    private Random random;
    
    public DataGenerator() {
        this.random = new Random();
    }
    
    public DataGenerator(long seed) {
        this.random = new Random(seed);
    }
    
    public List<Point> generateGridClusters(int gridSize, int pointsPerCluster, double clusterSpread, double clusterDistance) {
        List<Point> points = new ArrayList<>();
        String[] classNames = generateClassNames(gridSize * gridSize);
        
        int clusterIndex = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double centerX = (i + 0.5) * clusterDistance;
                double centerY = (j + 0.5) * clusterDistance;
                String className = classNames[clusterIndex];
                
                for (int p = 0; p < pointsPerCluster; p++) {
                    double x = centerX + random.nextGaussian() * clusterSpread;
                    double y = centerY + random.nextGaussian() * clusterSpread;
                    points.add(new Point(x, y, className));
                }
                clusterIndex++;
            }
        }
        
        return points;
    }
    
    public List<Point> generateTwoMoons(int pointsPerMoon, double noise, double moonDistance) {
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < pointsPerMoon; i++) {
            double angle = Math.PI * random.nextDouble();
            double radius = 1.0 + random.nextGaussian() * noise;
            
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle) + moonDistance/2;
            
            points.add(new Point(x, y, "Moon-A"));
        }
        
        for (int i = 0; i < pointsPerMoon; i++) {
            double angle = Math.PI * random.nextDouble();
            double radius = 1.0 + random.nextGaussian() * noise;
            
            double x = 1.0 - radius * Math.cos(angle);
            double y = -radius * Math.sin(angle) - moonDistance/2;
            
            points.add(new Point(x, y, "Moon-B"));
        }
        
        return points;
    }
    
    public List<Point> generateRandom(int numClasses, int totalPoints, double fieldSize) {
        List<Point> points = new ArrayList<>();
        String[] classNames = generateClassNames(numClasses);
        
        for (int i = 0; i < totalPoints; i++) {
            double x = random.nextDouble() * fieldSize;
            double y = random.nextDouble() * fieldSize;
            String className = classNames[random.nextInt(numClasses)];
            points.add(new Point(x, y, className));
        }
        
        return points;
    }
    
    private String[] generateClassNames(int numClasses) {
        String[] classNames = new String[numClasses];
        for (int i = 0; i < numClasses; i++) {
            classNames[i] = "Class-" + (char)('A' + i);
        }
        return classNames;
    }
    
    public static List<Point> normalizeData(List<Point> points) {
        if (points.isEmpty()) return points;
        
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        
        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        
        List<Point> normalized = new ArrayList<>();
        for (Point p : points) {
            double normX = (p.getX() - minX) / (maxX - minX);
            double normY = (p.getY() - minY) / (maxY - minY);
            normalized.add(new Point(normX, normY, p.getLabel()));
        }
        
        return normalized;
    }
}
