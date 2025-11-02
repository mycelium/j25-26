package org.example;

import java.util.*;

public class App {
    public static void main(String[] args) {
        DataGenerator generator = new DataGenerator(42);
        demonstrateGridClusters(generator);
        demonstrateTwoMoons(generator);
        demonstrateRandom(generator);
    }

    private static List<Point> createClassifiedPoints(List<Point> originalPoints, List<String> predictions) {
        List<Point> classifiedPoints = new ArrayList<>();
        for (int i = 0; i < originalPoints.size(); i++) {
            Point original = originalPoints.get(i);
            String predictedClass = predictions.get(i);
            classifiedPoints.add(new Point(original.getX(), original.getY(), predictedClass));
        }
        return classifiedPoints;
    }

    private static void demonstrateGridClusters(DataGenerator generator) {
        List<Point> trainingData = generator.generateGridClusters(3, 40, 0.05, 0.33);
        
        List<Point> testPoints = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < 20; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            testPoints.add(new Point(x, y));
        }
        KNN knn = new KNN(3);
        knn.fit(trainingData);
        
        List<String> predictions = knn.predict(testPoints);
        List<Point> classifiedPoints = createClassifiedPoints(testPoints, predictions);

        Canvas.displayVisualization(trainingData, classifiedPoints, "Grid Clusters");
    }

    private static void demonstrateTwoMoons(DataGenerator generator){
        List<Point> trainingData = generator.generateTwoMoons(200, 0.1, 0.5);
        trainingData = DataGenerator.normalizeData(trainingData);

        List<Point> testPoints = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < 20; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            testPoints.add(new Point(x, y));
        }

        KNN knn = new KNN(4);
        knn.fit(trainingData);
        
        List<String> predictions = knn.predict(testPoints);
        List<Point> classifiedPoints = createClassifiedPoints(testPoints, predictions);
        
        Canvas.displayVisualization(trainingData, classifiedPoints, "Two Moons" );
    }

    private static void demonstrateRandom(DataGenerator generator) {
        List<Point> trainingData = generator.generateRandom(5, 300, 1);

        List<Point> testPoints = new ArrayList<>();
        Random random = new Random(42);
        for (int i = 0; i < 20; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            testPoints.add(new Point(x, y));
        }
        
        KNN knn = new KNN(5);
        knn.fit(trainingData);

        List<String> predictions = knn.predict(testPoints);
        List<Point> classifiedPoints = createClassifiedPoints(testPoints, predictions);
        
        Canvas.displayVisualization(trainingData, classifiedPoints, "Random");
    }

}
