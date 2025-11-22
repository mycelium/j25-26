import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {
    private static int numThreads = 1;

    public static void setNumThreads (int num) { numThreads = num; }

    public static int getNumThreads() { return numThreads; }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
        try {
            if(firstMatrix == null || secondMatrix == null) {
                throw new IllegalArgumentException("Matrices cannot be null.");
            }

            if(firstMatrix.length == 0 || secondMatrix.length == 0) {
                throw new IllegalArgumentException("Matrices cannot be empty.");
            }
            int rowsInA = firstMatrix.length;
            int colsInA = firstMatrix[0].length;
            int colsInB = secondMatrix[0].length;

            if (secondMatrix.length != colsInA) {
                throw new IllegalArgumentException("Matrix sizes do not match for multiplication.");
            }

            double[][] result = new double[rowsInA][colsInB];
            double[][] secondMatrixTransposed = transpose(secondMatrix);

            List<Thread> threadList = new ArrayList<>();

            int rowsPerThread = Math.max(1, rowsInA / numThreads);

            for (int threadId = 0; threadId < numThreads; threadId++) {
                final int startRow = threadId * rowsPerThread;
                final int endRow = (threadId == numThreads - 1) ? rowsInA : (threadId + 1) * rowsPerThread;

                Thread thread = new Thread(() -> {
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < colsInB; j++) {
                            double sum = 0.0;
                            for (int k = 0; k < colsInA; k++) {
                                sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                            }
                            result[i][j] = sum;
                        }
                    }
                });

                threadList.add(thread);
                thread.start();
            }

            for (Thread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted before completion.", e);
                }
            }

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during matrix multiplication: " + e.getMessage(), e);
        }
    }

    // ================
    // кусок из лабы 1 (оптимизированное перемножение путем транспонирования матрицы 2
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        try {
            if (firstMatrix == null || secondMatrix == null) {
                throw new IllegalArgumentException("Matrices cannot be null.");
            }

            if(firstMatrix.length == 0 ||secondMatrix.length == 0) {
                throw new IllegalArgumentException("Matrices cannot be empty.");
            }

            int rowsInA = firstMatrix.length;
            int rowsInB = secondMatrix.length;
            int colsInA = firstMatrix[0].length;
            int colsInB = secondMatrix[0].length;

            if (secondMatrix.length != colsInA) {
                throw new IllegalArgumentException("Matrix sizes do not match for multiplication.");
            }

            double[][] result = new double[rowsInA][colsInB];
            double[][] secondMatrixTransposed = transpose(secondMatrix);

            for(int i = 0;i<rowsInA; i++) {
                for (int j = 0; j < colsInB; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < rowsInB; k++) {
                        sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                    }
                    result[i][j] = sum;
                }
            }

            return result;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error during matrix multiplication:" + e.getMessage(), e);
        }
    }

    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }

        return matrix;
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
    // ====================

    public static void main(String[] args) {
        try {
            int[] testSizes = {200, 500, 1000, 2000};
            int numOfTests = 10;
            setNumThreads(8);

            for (int size : testSizes) {
                System.out.printf("\n Testing with %dx%d matrix and %d threads", size, size, getNumThreads());

                double[][] A = generateMatrix(size, size);
                double[][] B = generateMatrix(size, size);

                long sizeParallelTotal = 0;
                long sizeSeqTotal = 0;

                for (int test = 0; test < numOfTests; test++) {
                    long parallelStart = System.currentTimeMillis();
                    double[][] parallelResult = multiplyParallel(A, B);
                    long parallelEnd = System.currentTimeMillis();

                    long seqStart = System.currentTimeMillis();
                    double[][] seqResult = multiply(A, B);
                    long seqEnd = System.currentTimeMillis();

                    long parallelTime = parallelEnd - parallelStart;
                    long seqTime = seqEnd - seqStart;

                    sizeParallelTotal += parallelTime;
                    sizeSeqTotal += seqTime;

                    System.out.println("\nTEST №" + (test + 1));
                    System.out.printf("\nParallel time: %d ms\n", parallelTime);
                    System.out.printf("Sequential time: %d ms\n", seqTime);

                    double avgParallelForSize = sizeParallelTotal / (double) numOfTests;
                    double avgSequentialForSize = sizeSeqTotal / (double) numOfTests;

                    System.out.printf("\n=== RESULTS for %dx%d matrix ===\n", size, size);
                    System.out.printf("Average parallel time: %.2f ms\n", avgParallelForSize);
                    System.out.printf("Average sequential time: %.2f ms\n", avgSequentialForSize);
                }
            }
        } catch (Exception e) {
            System.out.println("Error during testing: " + e.getMessage());
        }
    }
}