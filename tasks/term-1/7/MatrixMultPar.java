import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MatrixMultPar {

    private static final int OPTIMAL_THREAD_COUNT = 24;

     public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrices(firstMatrix, secondMatrix);
        int rowsFirst = firstMatrix.length;
        int colsFirst = firstMatrix[0].length;
        int colsSecond = secondMatrix[0].length;
        double[][] resultMatrix = new double[rowsFirst][colsSecond];

        double[][] transposedSecondMatrix = transposeMatrix(secondMatrix);

        for (int rowIndex = 0; rowIndex < rowsFirst; rowIndex++) {
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
        return resultMatrix;
    }


    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix.length < 400) {
            return multiply(firstMatrix, secondMatrix);
        }
        return multiplyParallelInternal(firstMatrix, secondMatrix, OPTIMAL_THREAD_COUNT);
    }

    static double[][] multiplyParallelInternal(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
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
        int benchmarkIterations = 5;

        for (int[] size : matrixSizes) {
            int dimensionN = size[0];
            int dimensionM = size[1];
            System.out.printf("%dx%d matrices%n", dimensionN, dimensionM);

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
