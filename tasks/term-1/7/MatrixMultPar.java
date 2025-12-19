import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

    public static double[][] multiplySingleThread(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        // Проверка совместимости размеров
        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                    "Несовместимые размеры матриц: " +
                            colsA + " столбцов в первой матрице ≠ " +
                            rowsB + " строк во второй матрице"
            );
        }

        double[][] result = new double[rowsA][colsB];

        double[][] secondMatrixT = transposeMatrix(secondMatrix);

        int numThreads = Runtime.getRuntime().availableProcessors();

        List<Thread> threads = new ArrayList<>();

        int rowsPerThread = rowsA / numThreads;
        int extraRows = rowsA % numThreads;

        int startRow = 0;
        for (int threadId = 0; threadId < numThreads; threadId++) {
            int threadRows = rowsPerThread + (threadId < extraRows ? 1 : 0);
            int endRow = startRow + threadRows;

            final int threadStart = startRow;
            final int threadEnd = endRow;

            Thread thread = new Thread(() -> {
                for (int i = threadStart; i < threadEnd; i++) {
                    double[] firstRow = firstMatrix[i];
                    double[] resultRow = result[i];

                    for (int j = 0; j < colsB; j++) {
                        double[] secondCol = secondMatrixT[j];
                        double sum = 0.0;

                        for (int k = 0; k < colsA; k++) {
                            sum += firstRow[k] * secondCol[k];
                        }
                        resultRow[j] = sum;
                    }
                }
            });

            threads.add(thread);
            thread.start();
            startRow = endRow;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Умножение прервано", e);
            }
        }

        return result;
    }

    private static double[][] transposeMatrix(double[][] matrix) {
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

    public static double[][] generateRandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = min + Math.random() * (max - min);
            }
        }
        return matrix;
    }

    public static boolean checkResultsEqual(double[][] result1, double[][] result2, double tolerance) {
        if (result1.length != result2.length || result1[0].length != result2[0].length) {
            return false;
        }

        for (int i = 0; i < result1.length; i++) {
            for (int j = 0; j < result1[0].length; j++) {
                if (Math.abs(result1[i][j] - result2[i][j]) > tolerance) {
                    return false;
                }
            }
        }

        return true;
    }

    public static int findOptimalThreads(double[][] matrixA, double[][] matrixB, int maxThreads, int iterations) {
        System.out.println("\n=== Подбор оптимального количества потоков ===");
        System.out.println("Тестируем от 1 до " + maxThreads + " потоков");
        System.out.println("Количество итераций: " + iterations);
        System.out.println();

        long bestTime = Long.MAX_VALUE;
        int optimalThreads = 1;

        for (int numThreads = 1; numThreads <= maxThreads; numThreads++) {
            long totalTime = 0;

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();

                multiplyParallelWithThreads(matrixA, matrixB, numThreads);

                long endTime = System.nanoTime();
                totalTime += (endTime - startTime) / 1_000_000; // в миллисекундах
            }

            long avgTime = totalTime / iterations;
            System.out.printf("Потоков: %2d | Среднее время: %6d мс%n", numThreads, avgTime);

            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = numThreads;
            }
        }

        System.out.println("\n--- РЕЗУЛЬТАТ ---");
        System.out.println("Оптимальное количество потоков: " + optimalThreads);
        System.out.println("Лучшее время: " + bestTime + " мс");

        return optimalThreads;
    }

    private static double[][] multiplyParallelWithThreads(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        double[][] result = new double[rowsA][colsB];
        double[][] secondMatrixT = transposeMatrix(secondMatrix);

        List<Thread> threads = new ArrayList<>();
        int rowsPerThread = rowsA / numThreads;
        int extraRows = rowsA % numThreads;

        int startRow = 0;
        for (int threadId = 0; threadId < numThreads; threadId++) {
            int threadRows = rowsPerThread + (threadId < extraRows ? 1 : 0);
            int endRow = startRow + threadRows;

            final int threadStart = startRow;
            final int threadEnd = endRow;

            Thread thread = new Thread(() -> {
                for (int i = threadStart; i < threadEnd; i++) {
                    double[] firstRow = firstMatrix[i];
                    double[] resultRow = result[i];

                    for (int j = 0; j < colsB; j++) {
                        double[] secondCol = secondMatrixT[j];
                        double sum = 0.0;

                        for (int k = 0; k < colsA; k++) {
                            sum += firstRow[k] * secondCol[k];
                        }
                        resultRow[j] = sum;
                    }
                }
            });

            threads.add(thread);
            thread.start();
            startRow = endRow;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(" ПАРАЛЛЕЛЬНОЕ УМНОЖЕНИЕ МАТРИЦ ");
        System.out.println("Доступно ядер процессора: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        int[] sizes = {500, 1000, 1500};
        int iterations = 5;

        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("РАЗМЕР МАТРИЦ: " + size + "x" + size);
            System.out.println("=".repeat(50));

            double[][] matrixA = generateRandomMatrix(size, size, 1, 100);
            double[][] matrixB = generateRandomMatrix(size, size, 1, 100);

            System.out.println("\n1. ОДНОПОТОЧНОЕ УМНОЖЕНИЕ:");
            long singleThreadTime = 0;
            double[][] singleThreadResult = null;

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                singleThreadResult = multiplySingleThread(matrixA, matrixB);
                long endTime = System.nanoTime();
                singleThreadTime += (endTime - startTime) / 1_000_000;
            }

            long avgSingleTime = singleThreadTime / iterations;
            System.out.println("Среднее время: " + avgSingleTime + " мс");

            System.out.println("\n2. ПАРАЛЛЕЛЬНОЕ УМНОЖЕНИЕ:");
            long parallelTime = 0;
            double[][] parallelResult = null;

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                parallelResult = multiplyParallel(matrixA, matrixB);
                long endTime = System.nanoTime();
                parallelTime += (endTime - startTime) / 1_000_000;
            }

            long avgParallelTime = parallelTime / iterations;
            System.out.println("Среднее время: " + avgParallelTime + " мс");

            System.out.println("\n3. СРАВНЕНИЕ ПРОИЗВОДИТЕЛЬНОСТИ:");
            double speedup = (double) avgSingleTime / avgParallelTime;
            System.out.printf("Ускорение: %.2f раз%n", speedup);

            System.out.println("\n4. ПРОВЕРКА КОРРЕКТНОСТИ:");
            boolean resultsEqual = checkResultsEqual(singleThreadResult, parallelResult, 0.0001);
            if (resultsEqual) {
                System.out.println("✓ Результаты совпадают");
            } else {
                System.out.println("✗ Результаты отличаются!");
            }

            System.out.println("\n5. ПОДБОР ОПТИМАЛЬНОГО КОЛИЧЕСТВА ПОТОКОВ:");
            int optimalThreads = findOptimalThreads(matrixA, matrixB,
                    Runtime.getRuntime().availableProcessors() * 2, 3);

            System.out.printf("\nДля матриц %dx%d оптимально использовать %d потоков%n",
                    size, size, optimalThreads);
        }

        System.out.println("ТЕСТИРОВАНИЕ ЗАВЕРШЕНО");
    }
}