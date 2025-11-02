import java.util.Random;

public class MatrixMult {

    // Метод генерации случайной матрицы
    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble();
            }
        }
        return matrix;
    }

    // Проверка, что матрица не null
    public static boolean isNotNull(double[][] matrix) {
        return matrix != null;
    }

    // Простой метод умножения матриц
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int n1 = firstMatrix.length;
        int m1 = firstMatrix[0].length;
        int n2 = secondMatrix.length;
        int m2 = secondMatrix[0].length;

        if (m1 != n2) {
            throw new IllegalArgumentException("Размерности матриц не совпадают для умножения");
        }

        double[][] result = new double[n1][m2];

        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < m2; j++) {
                double sum = 0;
                for (int k = 0; k < m1; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    // Метод блочного умножения матриц
    public static double[][] multiplyBlock(double[][] firstMatrix, double[][] secondMatrix, int blockSize) {
        int n1 = firstMatrix.length;
        int m1 = firstMatrix[0].length;
        int n2 = secondMatrix.length;
        int m2 = secondMatrix[0].length;

        if (m1 != n2) {
            throw new IllegalArgumentException("Размерности матриц не совпадают для умножения");
        }

        double[][] result = new double[n1][m2];

        for (int i = 0; i < n1; i += blockSize) {
            for (int j = 0; j < m2; j += blockSize) {
                for (int k = 0; k < m1; k += blockSize) {
                    for (int i1 = i; i1 < i + blockSize && i1 < n1; i1++) {
                        for (int j1 = j; j1 < j + blockSize && j1 < m2; j1++) {
                            double sum = result[i1][j1];
                            for (int k1 = k; k1 < k + blockSize && k1 < m1; k1++) {
                                sum += firstMatrix[i1][k1] * secondMatrix[k1][j1];
                            }
                            result[i1][j1] = sum;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int n = 1000;        // размер для теста
        int blockSize = 50; // размер блока
        int runs = 5;       // количество повторений для усреднения

        double[][] A = generateMatrix(n, n);
        double[][] B = generateMatrix(n, n);

        if (!isNotNull(A) || !isNotNull(B)) {
            System.out.println("Одна из матриц равна null. Программа завершена.");
            return;
        }

        long totalTimeSimple = 0;
        long totalTimeBlock = 0;

        System.out.println("Время каждого запуска простого умножения:");
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            double[][] resultSimple = multiply(A, B);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            totalTimeSimple += elapsed;
            System.out.println("Запуск " + (i + 1) + ": " + elapsed + " мс");
        }
        System.out.println("Среднее время простого умножения: " + (totalTimeSimple / (double) runs) + " мс\n");

        System.out.println("Время каждого запуска блочного умножения:");
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            double[][] resultBlock = multiplyBlock(A, B, blockSize);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            totalTimeBlock += elapsed;
            System.out.println("Запуск " + (i + 1) + ": " + elapsed + " мс");
        }
        System.out.println("Среднее время блочного умножения: " + (totalTimeBlock / (double) runs) + " мс");
    }
}
