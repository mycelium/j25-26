package org.example;

import java.util.*;

public class Canvas {
    public static void displayVisualization(List<Point> trainingPoints, List<Point> testPoints, String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
        
        printTextGrid(trainingPoints, testPoints);
        
        System.out.println("\nСтатистика:");
        System.out.println("Тренировочных точек: " + trainingPoints.size());
        System.out.println("Тестовых точек: " + testPoints.size());
        
        Map<String, Integer> classStats = new HashMap<>();
        for (Point p : trainingPoints) {
            classStats.put(p.getLabel(), classStats.getOrDefault(p.getLabel(), 0) + 1);
        }
        System.out.println("Классы в тренировочных данных: " + classStats);
    }
    
    private static void printTextGrid(List<Point> trainingPoints, List<Point> testPoints) {
        int gridSize = 40;
        char[][] grid = new char[gridSize][gridSize];
        
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = '·';
            }
        }
        
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        
        for (Point p : trainingPoints) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        for (Point p : testPoints) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        
        xRange = maxX - minX;
        yRange = maxY - minY;
        
        Map<String, Character> classSymbols = new HashMap<>();
        char[] symbols = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T'};
        int symbolIndex = 0;
        
        for (Point p : trainingPoints) {
            if (p.getLabel() != null) {
                if (!classSymbols.containsKey(p.getLabel())) {
                    classSymbols.put(p.getLabel(), symbols[symbolIndex++ % symbols.length]);
                }
                
                int x = (int) ((p.getX() - minX) / xRange * (gridSize - 1));
                int y = (int) ((p.getY() - minY) / yRange * (gridSize - 1));
                
                if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                    grid[y][x] = classSymbols.get(p.getLabel());
                }
            }
        }
        
        for (Point p : testPoints) {
            if (p.getLabel() != null) {
                int x = (int) ((p.getX() - minX) / xRange * (gridSize - 1));
                int y = (int) ((p.getY() - minY) / yRange * (gridSize - 1));
                
                if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                    grid[y][x] = 'X';
                }
            }
        }
        
        System.out.println("╔" + "═".repeat(gridSize * 2 + 1) + "╗");
        for (int i = 0; i < gridSize; i++) {
            System.out.print("║ ");
            for (int j = 0; j < gridSize; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println("║");
        }
        System.out.println("╚" + "═".repeat(gridSize * 2 + 1) + "╝");
        
        System.out.println("\nЛегенда:");
        for (Map.Entry<String, Character> entry : classSymbols.entrySet()) {
            System.out.println(entry.getValue() + " - " + entry.getKey());
        }
        System.out.println("X - тестовые точки");
    }
}
