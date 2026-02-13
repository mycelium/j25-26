import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class MatrixMultPar {

    private static final AtomicReference<Integer> optimalThreadCountRef = new AtomicReference<>();

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrices(firstMatrix, secondMatrix);
        int rowsFirst = firstMatrix.length;
        int colsFirst = firstMatrix[0].length;
        int colsSecond = secondMatrix[0].length;
        double[][] resultMatrix = new double[rowsFirst][colsSecond];

        for (int rowIndex = 0; rowIndex < rowsFirst; rowIndex++) {
            for (int kIndex = 0; kIndex < colsFirst; kIndex++) {
                double elementValue = firstMatrix[rowIndex][kIndex];
                for (int colIndex = 0; colIndex < colsSecond; colIndex++) {
                    resultMatrix[rowIndex][colIndex] += elementValue * secondMatrix[kIndex][colIndex];
                }
            }
        }
        return resultMatrix;
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        Integer threadCount = optimalThreadCountRef.get();
        if (threadCount == null) {
            threadCount = initializeOptimalThreadCount(firstMatrix, secondMatrix);
        }
        return multiplyParallelInternal(firstMatrix, secondMatrix, threadCount);
    }

    private static double[][] multiplyParallelInternal(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
        validateMatrices(firstMatrix, secondMatrix);
        int rowsFirst = firstMatrix.length;
        int colsFirst = firstMatrix[0].length;
        int colsSecond = secondMatrix[0].length;
        
        double[][] transposedSecondMatrix = transposeMatrix(secondMatrix);
        double[][] resultMatrix = new double[rowsFirst][colsSecond];

        int actualThreadCount = Math.min(threadCount, rowsFirst);
        if (actualThreadCount <= 0) actualThreadCount = 1;

        int rowsPerThread = (rowsFirst + actualThreadCount - 1) / actualThreadCount;
        ExecutorService threadPool = Executors.newFixedThreadPool(actualThreadCount);
        CountDownLatch completionLatch = new CountDownLatch(actualThreadCount);

        for (int threadIndex = 0; threadIndex < actualThreadCount; threadIndex++) {
            int startRow = threadIndex * rowsPerThread;
            int endRow = Math.min(startRow + rowsPerThread, rowsFirst);
            if (startRow >= rowsFirst) break;

            final int threadStartRow = startRow;
            final int threadEndRow = endRow;
            threadPool.submit(() -> {
                try {
                    for (int rowIndex = threadStartRow; rowIndex < threadEndRow; rowIndex++) {
                        double[] currentRowFirst = firstMatrix[rowIndex];
                        double[] currentResultRow = resultMatrix[rowIndex];
                        for (int colIndex = 0; colIndex < colsSecond; colIndex++) {
                            double[] currentColumnSecond = transposedSecondMatrix[colIndex];
                            double sum = 0.0;
                            for (int kIndex = 0; kIndex < colsFirst; kIndex++) {
                                sum += currentRowFirst[kIndex] * currentColumnSecond[kIndex];
                            }
                            currentResultRow[colIndex] = sum;
                        }
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matrix multiplication interrupted", e);
        } finally {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return resultMatrix;
    }

    private static int initializeOptimalThreadCount(double[][] firstMatrix, double[][] secondMatrix) {
        return optimalThreadCountRef.updateAndGet(existing -> {
            if (existing != null) return existing;

            int maxThreads = Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);
            int testRows = Math.max(300, Math.min(1000, firstMatrix.length));
            int testCols = Math.max(300, Math.min(1000, firstMatrix[0].length));
            int commonDimension = testCols;

            double[][] testMatrixA = generateRandomMatrix(testRows, commonDimension);
            double[][] testMatrixB = generateRandomMatrix(commonDimension, testCols);

            long bestExecutionTime = Long.MAX_VALUE;
            int bestThreadCount = 1;
            int benchmarkTrials = 10;

            for (int threads = 1; threads <= maxThreads; threads++) {
                long totalTime = 0;
                for (int trial = 0; trial < benchmarkTrials; trial++) {
                    long startTime = System.nanoTime();
                    multiplyParallelInternal(testMatrixA, testMatrixB, threads);
                    long endTime = System.nanoTime();
                    totalTime += (endTime - startTime);
                }
                long averageTime = totalTime / benchmarkTrials;
                if (averageTime < bestExecutionTime) {
                    bestExecutionTime = averageTime;
                    bestThreadCount = threads;
                }
            }

            System.out.println("Optimal thread count: " + bestThreadCount + "\n");
            return bestThreadCount;
        });
    }

    private static void validateMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null)
            throw new IllegalArgumentException("Input matrices must not be null");
        if (firstMatrix.length == 0 || firstMatrix[0].length == 0 || 
            secondMatrix.length == 0 || secondMatrix[0].length == 0)
            throw new IllegalArgumentException("Input matrices must not be empty");
        if (firstMatrix[0].length != secondMatrix.length)
            throw new IllegalArgumentException(
                "Matrix dimensions incompatible: columns of first matrix (" + firstMatrix[0].length +
                ") must equal rows of second matrix (" + secondMatrix.length + ")"
            );
    }

    private static double[][] transposeMatrix(double[][] matrix) {
        int originalRows = matrix.length;
        int originalCols = matrix[0].length;
        double[][] transposedMatrix = new double[originalCols][originalRows];
        for (int rowIndex = 0; rowIndex < originalRows; rowIndex++) {
            for (int colIndex = 0; colIndex < originalCols; colIndex++) {
                transposedMatrix[colIndex][rowIndex] = matrix[rowIndex][colIndex];
            }
        }
        return transposedMatrix;
    }

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            for (int colIndex = 0; colIndex < cols; colIndex++) {
                matrix[rowIndex][colIndex] = Math.random();
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        int[][] matrixSizes = {{1000, 1000}, {1500, 1500}, {2000, 2000}};
        int benchmarkIterations = 10;

        for (int[] size : matrixSizes) {
            int dimensionN = size[0];
            int dimensionM = size[1];
            System.out.printf("matrices%n", dimensionN, dimensionM);

            long totalSerialTime = 0;
            long totalParallelTime = 0;

            for (int iteration = 0; iteration < benchmarkIterations; iteration++) {
                double[][] matrixA = generateRandomMatrix(dimensionN, dimensionM);
                double[][] matrixB = generateRandomMatrix(dimensionM, dimensionN);

                long startTime = System.nanoTime();
                multiply(matrixA, matrixB);
                totalSerialTime += System.nanoTime() - startTime;

                startTime = System.nanoTime();
                multiplyParallel(matrixA, matrixB);
                totalParallelTime += System.nanoTime() - startTime;
            }

            double averageSerialMs = totalSerialTime / (benchmarkIterations * 1_000_000.0);
            double averageParallelMs = totalParallelTime / (benchmarkIterations * 1_000_000.0);
            double speedupFactor = averageSerialMs / averageParallelMs;

            System.out.printf("Single-threaded:   %.2f ms%n", averageSerialMs);
            System.out.printf("Multi-threaded:    %.2f ms%n", averageParallelMs);
            System.out.printf("Speedup:           %.2f x%n%n", speedupFactor);
        }
    }
}
