
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MatrixMultPar {

    private static final AtomicReference<Integer> optimalThreadCountRef = new AtomicReference<>();

    public static double[][] multiply(double[][] a, double[][] b) {
        validate(a, b);
        int rowsA = a.length, colsA = a[0].length, colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int k = 0; k < colsA; k++) {
                double aik = a[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += aik * b[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] multiplyParallel(double[][] a, double[][] b) {
        Integer threadCount = optimalThreadCountRef.get();
        if (threadCount == null) {
            threadCount = initializeOptimalThreadCount(a, b);
        }
        return multiplyParallelInternal(a, b, threadCount);
    }

    private static double[][] multiplyParallelInternal(double[][] a, double[][] b, int threadCount) {
        validate(a, b);
        int m = a.length, n = a[0].length, p = b[0].length;
        double[][] bTransposed = transpose(b);
        double[][] result = new double[m][p];

        int actualThreads = Math.min(threadCount, m);
        if (actualThreads <= 0) actualThreads = 1;

        int rowsPerThread = (m + actualThreads - 1) / actualThreads;
        ExecutorService executor = Executors.newFixedThreadPool(actualThreads);
        CountDownLatch latch = new CountDownLatch(actualThreads);

        for (int t = 0; t < actualThreads; t++) {
            int startRow = t * rowsPerThread;
            int endRow = Math.min(startRow + rowsPerThread, m);
            if (startRow >= m) break;

            final int finalStart = startRow;
            final int finalEnd = endRow;
            executor.submit(() -> {
                try {
                    for (int i = finalStart; i < finalEnd; i++) {
                        double[] rowA = a[i];
                        double[] rowResult = result[i];
                        for (int j = 0; j < p; j++) {
                            double[] colB = bTransposed[j];
                            double sum = 0.0;
                            for (int k = 0; k < n; k++) {
                                sum += rowA[k] * colB[k];
                            }
                            rowResult[j] = sum;
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execution interrupted", e);
        } finally {
            executor.shutdown();
        }

        return result;
    }

    private static int initializeOptimalThreadCount(double[][] a, double[][] b) {
        return optimalThreadCountRef.updateAndGet(existing -> {
            if (existing != null) return existing;

            int maxThreads = Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);
            int rows = Math.max(300, Math.min(1000, a.length));
            int cols = Math.max(300, Math.min(1000, a[0].length));
            int common = cols;

            double[][] testA = generateRandomMatrix(rows, common);
            double[][] testB = generateRandomMatrix(common, cols);

            System.out.println("Tuning optimal thread count on " + rows + "x" + cols + " test matrix...");

            long bestTime = Long.MAX_VALUE;
            int bestThreads = 1;
            int trials = 10;

            for (int threads = 1; threads <= maxThreads; threads++) {
                long totalTime = 0;
                for (int i = 0; i < trials; i++) {
                    long start = System.nanoTime();
                    multiplyParallelInternal(testA, testB, threads);
                    long end = System.nanoTime();
                    totalTime += (end - start);
                }
                long avg = totalTime / trials;
                if (avg < bestTime) {
                    bestTime = avg;
                    bestThreads = threads;
                }
            }

            System.out.println("Optimal thread count: " + bestThreads + "\n");
            return bestThreads;
        });
    }

    private static void validate(double[][] a, double[][] b) {
        if (a == null || b == null)
            throw new IllegalArgumentException("Matrices must not be null");
        if (a.length == 0 || a[0].length == 0 || b.length == 0 || b[0].length == 0)
            throw new IllegalArgumentException("Matrices must not be empty");
        if (a[0].length != b.length)
            throw new IllegalArgumentException(
                "Incompatible dimensions: columns of first (" + a[0].length +
                ") != rows of second (" + b.length + ")"
            );
    }

    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
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
        // Warm-up run
        multiplyParallel(generateRandomMatrix(1000, 1000), generateRandomMatrix(1000, 1000));

        int[][] sizes = {{1000, 1000}, {1500, 1500}, {2000, 2000}};
        int testCount = 10;

        for (int[] size : sizes) {
            int n = size[0], m = size[1];
            System.out.printf("Benchmarking %dx%d matrices%n", n, m);

            long serialTime = 0, parallelTime = 0;

            for (int i = 0; i < testCount; i++) {
                double[][] A = generateRandomMatrix(n, m);
                double[][] B = generateRandomMatrix(m, n);

                long start = System.nanoTime();
                multiply(A, B);
                serialTime += System.nanoTime() - start;

                start = System.nanoTime();
                multiplyParallel(A, B);
                parallelTime += System.nanoTime() - start;
            }

            double avgSerialMs = serialTime / (testCount * 1_000_000.0);
            double avgParallelMs = parallelTime / (testCount * 1_000_000.0);
            double speedup = avgSerialMs / avgParallelMs;

            System.out.printf("Single-threaded:   %.2f ms%n", avgSerialMs);
            System.out.printf("Multi-threaded:    %.2f ms%n", avgParallelMs);
            System.out.printf("Speedup:           %.2f%n%n", speedup);
        }
    }

}
