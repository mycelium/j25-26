package org.example;

import java.util.Random;

public class MatrixTestGenerator {
    
    public static double[][] generateMatrix(int rows, int cols, double minValue, double maxValue) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = minValue + (maxValue - minValue) * random.nextDouble();
            }
        }
        
        return matrix;
    }
    
    public static double[][] generateIntMatrix(int rows, int cols, int minValue, int maxValue) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(maxValue - minValue + 1) + minValue;
            }
        }
        
        return matrix;
    }
    
    public static void main(String[] args) {

        System.out.println("\n=== Test: matrice (500x10000 * 10000x800) ===");
        double[][] m9 = generateIntMatrix(500, 10000, 1, 100);
        double[][] m10 = generateIntMatrix(10000, 800, 1, 100);
        MatrixMult.multiply(m9, m10);
    }
}

