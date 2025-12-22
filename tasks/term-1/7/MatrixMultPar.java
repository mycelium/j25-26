import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrix(firstMatrix, secondMatrix);

        int firstMatrixRows = firstMatrix.length;
        int firstMatrixColumns = firstMatrix[0].length;
        int secondMatrixColumns = secondMatrix[0].length;

        double[][] resultMatrix = new double[firstMatrixRows][secondMatrixColumns];

        for (int i = 0; i < firstMatrixRows; i++) {
            for (int k = 0; k < firstMatrixColumns; k++) {
                double cache = firstMatrix[i][k];
                for (int j = 0; j < secondMatrixColumns; j++) {
                    resultMatrix[i][j] += cache * secondMatrix[k][j];
                }
            }
        }

        return resultMatrix;
    }

    private static volatile Integer optimalThreadCount = null;
    private static final Object lock = new Object();

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        if (optimalThreadCount == null) {
            initializeOptimalThreadCount(firstMatrix, secondMatrix);
        }
        return multiplyParallelInternal(firstMatrix, secondMatrix, optimalThreadCount);
    }

    private static double[][] multiplyParallelInternal(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
        validateMatrix(firstMatrix, secondMatrix);

        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        double[][] secondTransposed = transpose(secondMatrix);
        double[][] result = new double[m][p];

        threadCount = Math.min(threadCount, m);
        if (threadCount <= 0) threadCount = 1;

        int rowsPerThread = (m + threadCount - 1) / threadCount;
        List<Thread> threads = new ArrayList<>(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, m);
            if (startRow >= m) break;

            Thread worker = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    double[] rowA = firstMatrix[i];
                    double[] rowResult = result[i];
                    for (int j = 0; j < p; j++) {
                        double[] colB = secondTransposed[j];
                        double sum = 0.0;
                        for (int k = 0; k < n; k++) {
                            sum += rowA[k] * colB[k];
                        }
                        rowResult[j] = sum;
                    }
                }
            });

            threads.add(worker);
            worker.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Поток прерван", e);
            }
        }

        return result;
    }

    private static void initializeOptimalThreadCount(double[][] A, double[][] B) {
        synchronized (lock) {
            if (optimalThreadCount != null) return;

            int maxThreadsToTest = Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);
            int rows = Math.max(300, Math.min(1000, A.length));
            int cols = Math.max(300, Math.min(1000, A[0].length));
            int common = cols;

            double[][] testA = generateRandomMatrix(rows, common);
            double[][] testB = generateRandomMatrix(common, cols);

            long bestTime = Long.MAX_VALUE;
            int bestThreads = 1;
            int trials = 10;

            System.out.println("Подбор оптимального числа потоков на тестовой матрице " + rows + "x" + cols + "...");

            for (int threads = 1; threads <= maxThreadsToTest; threads++) {
                long totalTime = 0;
                for (int i = 0; i < trials; i++) {
                    long start = System.nanoTime();
                    multiplyParallelInternal(testA, testB, threads);
                    long end = System.nanoTime();
                    totalTime += (end - start);
                }
                long avgNs = totalTime / trials;
                if (avgNs < bestTime) {
                    bestTime = avgNs;
                    bestThreads = threads;
                }
            }

            optimalThreadCount = bestThreads;
            System.out.println("Оптимальное число потоков: " + optimalThreadCount + "\n");
        }
    }

    private static void validateMatrix(double[][] first, double[][] second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Матрицы не должны быть null");
        }
        if (first.length == 0 || first[0].length == 0 || second.length == 0 || second[0].length == 0) {
            throw new IllegalArgumentException("Матрицы не должны быть пустыми");
        }
        if (first[0].length != second.length) {
            throw new IllegalArgumentException(
                "Несовместимые размеры: столбцы первой (" + first[0].length +
                ") != строки второй (" + second.length + ")"
            );
        }
    }

    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        double[][] testA = generateRandomMatrix(1000, 1000);
        double[][] testB = generateRandomMatrix(1000, 1000);
        multiplyParallel(testA, testB);


        int[][] sizes = {
            {1000, 1000},
            {1500, 1500},
            {2000, 2000},
        };
        int testCount = 10;

        for (int[] size : sizes) {
            int n = size[0];
            int m = size[1];

            System.out.printf("Тестирование матриц %dx%d\n", n, m);

            long serialTime = 0;
            long parallelTime = 0;

            for (int i = 0; i < testCount; i++) {
                double[][] A = generateRandomMatrix(n, m);
                double[][] B = generateRandomMatrix(m, n);

                long start = System.nanoTime();
                multiply(A, B);
                long end = System.nanoTime();
                serialTime += (end - start);

                start = System.nanoTime();
                multiplyParallel(A, B);
                end = System.nanoTime();
                parallelTime += (end - start);
            }

            double avgSerialMs = serialTime / (testCount * 1_000_000.0);
            double avgParallelMs = parallelTime / (testCount * 1_000_000.0);
            double speedup = avgSerialMs / avgParallelMs;

            System.out.printf("Однопоточно:   %.2f мс%n", avgSerialMs);
            System.out.printf("Многопоточно:  %.2f мс%n", avgParallelMs);
            System.out.printf("Коэффициент ускорения:     %.2f%n%n", speedup);
        }
    }
}