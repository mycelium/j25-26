import java.util.*;
import java.util.concurrent.*;

public class MatrixMultPar {

    private static int lastUsedThreads = 1;
    private static final Map<Integer, Integer> optimalThreadsCache = new HashMap<>();
    private static final Map<Integer, ExecutorService> executorsMap = new HashMap<>();
    private static final int REPETITIONS = 3;

    public static double[][] multiplyParallel(double[][] matrixA, double[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;
        int colsB = matrixB[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                    "Cannot multiply: columns of A (" + colsA + ") != rows of B (" + rowsB + ")"
            );
        }


        double[][] matrixBTranspose = transpose(matrixB);

        int threadCount = (rowsA < 2000)
                ? optimalThreadsCache.computeIfAbsent(rowsA, MatrixMultPar::calculateOptimalThreads)
                : Runtime.getRuntime().availableProcessors();

        lastUsedThreads = threadCount;

        return multiplyParallel(matrixA, matrixBTranspose, threadCount);
    }

    private static double[][] multiplyParallel(double[][] matrixA, double[][] matrixBTransposed, int threadCount) {
        double[][] product = new double[matrixA.length][matrixBTransposed.length];

        ExecutorService executor = executorsMap.computeIfAbsent(threadCount,
                n -> Executors.newFixedThreadPool(n));

        int rowsPerThread = (int) Math.ceil((double) matrixA.length / threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, matrixA.length);

            futures.add(executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < matrixBTransposed.length; j++) {
                        double sum = 0;
                        for (int k = 0; k < matrixA[0].length; k++) {
                            sum += matrixA[i][k] * matrixBTransposed[j][k];
                        }
                        product[i][j] = sum;
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return product;
    }

    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                transposed[j][i] = matrix[i][j];
        return transposed;
    }

    public static double[][] createMatrix(int rows, int cols, int min, int max) {
        double[][] matrix = new double[rows][cols];
        Random rnd = new Random();
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix[i][j] = rnd.nextDouble() * (max - min) + min;
        return matrix;
    }

    private static int calculateOptimalThreads(int size) {
        int available = Runtime.getRuntime().availableProcessors();
        double[][] testA = createMatrix(size, size, 0, 10);
        double[][] testB = createMatrix(size, size, 0, 10);

        double bestTime = Double.MAX_VALUE;
        int bestThreads = 1;

        System.out.printf("Testing threads for %dx%d matrix:%n", size, size);

        for (int threads = 1; threads <= available * 2; threads++) {
            long totalTime = 0;


            for (int w = 0; w < 2; w++)
                multiplyParallel(testA, testB, threads);


            for (int r = 0; r < REPETITIONS; r++) {
                long start = System.nanoTime();
                multiplyParallel(testA, testB, threads);
                long end = System.nanoTime();
                totalTime += (end - start);
            }

            double avgMs = totalTime / (double) REPETITIONS / 1_000_000.0;
            if (avgMs < bestTime) {
                bestTime = avgMs;
                bestThreads = threads;
            }
        }

        System.out.printf("Best for %dx%d: %d threads (%.2f ms)%n%n", size, size, bestThreads, bestTime);
        return bestThreads;
    }


    public static void benchmark(int size, int repetitions) {
        System.out.println("Benchmarking size: " + size + "x" + size);

        double[][] m1 = createMatrix(size, size, 0, 10);
        double[][] m2 = createMatrix(size, size, 0, 10);


        for (int i = 0; i < 2; i++)
            multiplyParallel(m1, m2);

        long totalTime = 0;
        for (int i = 0; i < repetitions; i++) {
            long start = System.nanoTime();
            multiplyParallel(m1, m2);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        double avgMs = totalTime / (double) repetitions / 1_000_000.0;
        System.out.printf("Average time (%d threads): %.3f ms%n%n", lastUsedThreads, avgMs);
    }

    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000, 2000};

        try {
            for (int size : sizes) {
                benchmark(size, 5);
            }
        } finally {

            for (ExecutorService executor : executorsMap.values()) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            }
        }
    }
}
