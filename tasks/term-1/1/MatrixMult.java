package com.matrix;

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        //проверка условия перемножения матриц
        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + colsA + " != " + rowsB
            );
        }
        
        double[][] result = new double[rowsA][colsB];
        
        //базовая версия
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }
        
        return result;
    }
    
    //оптимизированная версия 
    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + colsA + " != " + rowsB
            );
        }
        
        double[][] result = new double[rowsA][colsB];
        
        //транспонирование второй матрицы
        double[][] secondMatrixT = new double[colsB][rowsB];
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                secondMatrixT[j][i] = secondMatrix[i][j];
            }
        }
        
        //умножение на транспонированную матрицу
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrixT[j][k];
                }
                result[i][j] = sum;
            }
        }
        
        return result;
    }
    
 //тестирование версий и измерение времени
    public static void main(String[] args) {
        
        int[] sizes = {100, 500, 1000, 2000};  
        
        for (int size : sizes) {
            System.out.println("\n=== Тестирование матриц " + size + "x" + size + " ===");
            
            double[][] matrixA = generateRandomMatrix(size, size);
            double[][] matrixB = generateRandomMatrix(size, size);
            
            //базовая версия
            long startTime = System.currentTimeMillis();
            double[][] result1 = multiply(matrixA, matrixB);
            long endTime = System.currentTimeMillis();
            long basicTime = endTime - startTime;  
            System.out.println("Базовая версия: " + basicTime + " мс");
            
            //оптимизированная версия
            startTime = System.currentTimeMillis();
            double[][] result2 = multiplyOptimized(matrixA, matrixB);
            endTime = System.currentTimeMillis();
            long optimizedTime = endTime - startTime;  
            System.out.println("Оптимизированная версия: " + optimizedTime + " мс");
            
            //проверка, что результат умножения одинаковый у разных версий
            boolean isCorrect = matricesEqual(result1, result2, 1e-10);
            System.out.println("Результаты идентичны: " + isCorrect);
            
            if (optimizedTime > 0) {
                double speedup = (double) basicTime / optimizedTime;
                System.out.printf("Ускорение: %.2fx\n", speedup);
                
                if (speedup > 1.0) {
                    System.out.printf("Оптимизированная версия быстрее на %.1f%%\n", (speedup - 1.0) * 100);
                } else if (speedup < 1.0) {
                    System.out.printf("Базовая версия быстрее на %.1f%%\n", (1.0 - speedup) * 100);
                } else {
                    System.out.println("Производительность одинаковая");
                }
            }
        }
        
        //testCorrectness();
    }
    
    //генератор случайных матриц
    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }
    
    //сравнение матриц (с использованием сравнения с допустимой погрешности, а не a[i][j] == b[i][j] т.к. числа с плавающей точкой)
    public static boolean matricesEqual(double[][] a, double[][] b, double epsilon) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }
    
    //проверка корректности на маленьких матрицах
    /*public static void testCorrectness() {
        System.out.println("\n=== проверка ===");
        
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6}
        };
        
        double[][] B = {
            {7, 8},
            {9, 10},
            {11, 12}
        };
        
        double[][] expected = {
            {58, 64},
            {139, 154}
        };
        
        double[][] result1 = multiply(A, B);
        double[][] result2 = multiplyOptimized(A, B);
        
        System.out.println("Ожидаемый результат:");
        printMatrix(expected);
        
        System.out.println("Результат базовой версии:");
        printMatrix(result1);
        
        System.out.println("Результат оптимизированной версии:");
        printMatrix(result2);
        
        boolean test1 = matricesEqual(result1, expected, 1e-10);
        boolean test2 = matricesEqual(result2, expected, 1e-10);
        
        System.out.println("Базовая версия корректна: " + test1);
        System.out.println("Оптимизированная версия корректна: " + test2);
    }
    
    //метод для печати матрицы
    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%8.2f", value);
            }
            System.out.println();
        }
    }*/
}
