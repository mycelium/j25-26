package org.example;

import java.util.*;

public class App {
    public static void main(String[] args) {
        demonstrateSimpleCase();
        
        DataGenerator generator = new DataGenerator(42);
        demonstrateGridClusters(generator);
        demonstrateTwoMoons(generator);
        demonstrateRandom(generator);
    }
    
    private static void demonstrateSimpleCase() {
        System.out.println("=== Демонстрация 1: Простой случай ===");
        
        List<Point> trainingData = new ArrayList<>();
        
        trainingData.add(new Point(1.0, 1.0, "Class-A"));
        trainingData.add(new Point(1.2, 1.1, "Class-A"));
        trainingData.add(new Point(0.8, 0.9, "Class-A"));
        
        trainingData.add(new Point(3.0, 3.0, "Class-B"));
        trainingData.add(new Point(3.2, 2.9, "Class-B"));
        trainingData.add(new Point(2.8, 3.1, "Class-B"));
        
        trainingData.add(new Point(1.0, 3.0, "Class-C"));
        trainingData.add(new Point(1.1, 2.9, "Class-C"));
        trainingData.add(new Point(0.9, 3.1, "Class-C"));
        
        Point testPoint = new Point(1.5, 1.5);
        
        KNN knn = new KNN(3);
        knn.fit(trainingData);
        String predictedClass = knn.predict(testPoint);
        
        System.out.println("Тестовая точка: " + testPoint);
        System.out.println("Предсказанный класс: " + predictedClass);
        
        testPoint.setLabel(predictedClass);
        List<Point> testPoints = Arrays.asList(testPoint);
        Canvas.displayVisualization(trainingData, testPoints, "Простой пример KNN");
    }
    
    private static void demonstrateGridClusters(DataGenerator generator) {
        System.out.println("\n=== Демонстрация 2: Сетка кластеров ===");
        
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

        System.out.println("Классифицировано " + testPoints.size() + " тестовых точек");
        Canvas.displayVisualization(trainingData, classifiedPoints, "Сетка кластеров");
    }
    
    private static void demonstrateTwoMoons(DataGenerator generator) {
        System.out.println("\n=== Демонстрация 3: Два полумесяца ===");
        
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
        
        System.out.println("Классифицировано " + testPoints.size() + " тестовых точек");
        Canvas.displayVisualization(trainingData, classifiedPoints, "Два полумесяца");
    }
    
    private static void demonstrateRandom(DataGenerator generator) {
        System.out.println("\n=== Демонстрация 4: Случайные данные ===");
        
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
        
        System.out.println("Классифицировано " + testPoints.size() + " тестовых точек");
        Canvas.displayVisualization(trainingData, classifiedPoints, "Случайные данные");
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
}
