package org.example;

import java.util.*;

public class KNNVisualizer {

    public void visualize(KNNClassifier classifier, Point unknownPoint) {
        List<Point> trainingData = classifier.getTrainingData();
        Set<String> classes = classifier.getClasses();

        System.out.println("\n" + "-".repeat(50));
        System.out.println("KNN CLASSIFICATION VISUALIZATION");
        System.out.println("-".repeat(50));

        // находим границы данных
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Point p : trainingData) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        if (unknownPoint != null) {
            minX = Math.min(minX, unknownPoint.getX());
            maxX = Math.max(maxX, unknownPoint.getX());
            minY = Math.min(minY, unknownPoint.getY());
            maxY = Math.max(maxY, unknownPoint.getY());
        }

        int gridSize = 20;
        char[][] grid = createTextGrid(minX, maxX, minY, maxY, gridSize);

        for (Point p : trainingData) {
            plotPoint(grid, p, minX, maxX, minY, maxY, gridSize, getClassSymbol(p.getLabel()));
        }

        if (unknownPoint != null) {
            String predictedClass = classifier.classify(unknownPoint);
            plotPoint(grid, unknownPoint, minX, maxX, minY, maxY, gridSize, 'X');
            System.out.printf("Unknown point (%.1f, %.1f) classified as: %s%n",
                    unknownPoint.getX(), unknownPoint.getY(), predictedClass);
        }

        printGrid(grid, minX, maxX, minY, maxY, gridSize);

        printLegend(classes);
    }

    private char[][] createTextGrid(double minX, double maxX, double minY, double maxY, int size) {
        char[][] grid = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = '·';
            }
        }
        return grid;
    }

    private void plotPoint(char[][] grid, Point point, double minX, double maxX,
                           double minY, double maxY, int gridSize, char symbol) {
        int x = (int) ((point.getX() - minX) / (maxX - minX) * (gridSize - 1));
        int y = (int) ((point.getY() - minY) / (maxY - minY) * (gridSize - 1));

        // инвертируем Y для корректного отображения
        y = gridSize - 1 - y;

        if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
            grid[y][x] = symbol;
        }
    }

    private void printGrid(char[][] grid, double minX, double maxX, double minY, double maxY, int size) {
        System.out.println("\nCoordinate System:");
        System.out.printf("X: [%.1f - %.1f], Y: [%.1f - %.1f]%n%n", minX, maxX, minY, maxY);

        for (int i = 0; i < size; i++) {
            System.out.print("  ");
            for (int j = 0; j < size; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private char getClassSymbol(String className) {
        if (className == null) return '?';

        switch (className) {
            case "Class-A": return 'A';
            case "Class-B": return 'B';
            case "Class-C": return 'C';
            case "Class-D": return 'D';
            default: return className.charAt(0);
        }
    }

    private void printLegend(Set<String> classes) {
        System.out.println("Legend:");
        for (String className : classes) {
            System.out.printf("  %c = %s%n", getClassSymbol(className), className);
        }
        System.out.println("  X = Unknown point to classify");
        System.out.println("  · = Empty space");
        System.out.println("~".repeat(50) + "\n");
    }
}