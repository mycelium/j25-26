import java.util.Random;

public class MatrixMultPar {

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

    // Простой метод умножения матриц (однопоточный)
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

    // Метод блочного умножения матриц (из 1 лабораторной)
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

    // Параллельное умножение
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
        int n1 = firstMatrix.length;
        int m1 = firstMatrix[0].length;
        int n2 = secondMatrix.length;
        int m2 = secondMatrix[0].length;

        if (m1 != n2) {
            throw new IllegalArgumentException("Размерности матриц не совпадают для умножения");
        }

        double[][] result = new double[n1][m2];

        Thread[] threads = new Thread[threadCount];

        int rowsPerThread = n1 / threadCount;
        int remainingRows = n1 % threadCount;

        int currentRow = 0;
        for (int t = 0; t < threadCount; t++) {
            int startRow = currentRow;
            int rowsForThisThread = rowsPerThread;
            if (t < remainingRows) {
                rowsForThisThread = rowsForThisThread + 1;
            }
            int endRow = startRow + rowsForThisThread;

            Runnable task = new MatrixTask(firstMatrix, secondMatrix, result, startRow, endRow);
            threads[t] = new Thread(task);
            threads[t].start();

            currentRow = endRow;
        }

        for (int t = 0; t < threadCount; t++) {
            try {
                threads[t].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int n = 1000;       // размер для теста
        int blockSize = 50; // размер блока
        int runs = 5;       // количество повторений для усреднения

        double[][] A = generateMatrix(n, n);
        double[][] B = generateMatrix(n, n);

        if (!isNotNull(A) || !isNotNull(B)) {
            System.out.println("Одна из матриц равна null. Программа завершена.");
            return;
        }

        // однопоточное умножение
        long totalTimeSimple = 0;
        System.out.println("Время каждого запуска простого умножения:");
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            double[][] resultSimple = multiply(A, B);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            totalTimeSimple += elapsed;
            System.out.println("Запуск " + (i + 1) + ": " + elapsed + " мс");
        }
        System.out.println("Среднее время простого умножения: " +
                (totalTimeSimple / (double) runs) + " мс\n");

        // блочное умножение(из 1 лабораторной)
        long totalTimeBlock = 0;
        System.out.println("Время каждого запуска блочного умножения:");
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            double[][] resultBlock = multiplyBlock(A, B, blockSize);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            totalTimeBlock += elapsed;
            System.out.println("Запуск " + (i + 1) + ": " + elapsed + " мс");
        }
        System.out.println("Среднее время блочного умножения: " +
                (totalTimeBlock / (double) runs) + " мс\n");

        // параллельное
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Доступно процессоров: " + cores);
        System.out.println("Параллельное умножение (сравнение времени):");


        for (int threads = 1; threads <= cores * 2; threads = threads * 2) {
            long totalTimePar = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.currentTimeMillis();
                double[][] resultPar = multiplyParallel(A, B, threads);
                long end = System.currentTimeMillis();
                long elapsed = end - start;
                totalTimePar += elapsed;
            }
            double avg = totalTimePar / (double) runs;
            System.out.println("Потоков: " + threads + ", среднее время: " + avg + " мс");
        }

        int threads3 = 3;
        long totalTimePar3 = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            double[][] resultPar = multiplyParallel(A, B, threads3);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            totalTimePar3 += elapsed;
        }
        double avg3 = totalTimePar3 / (double) runs;
        System.out.println("Потоков: " + threads3 + ", среднее время: " + avg3 + " мс");
    }
}

// Класс задачи для одного потока
class MatrixTask implements Runnable {

    private double[][] a;
    private double[][] b;
    private double[][] c;
    private int startRow;
    private int endRow;

    public MatrixTask(double[][] a, double[][] b, double[][] c, int startRow, int endRow) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        int m = a[0].length;
        int p = b[0].length;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < m; k++) {
                    sum += a[i][k] * b[k][j];
                }
                c[i][j] = sum;
            }
        }
    }
}