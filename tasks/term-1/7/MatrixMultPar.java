import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

    private static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        return multiplyParallel(firstMatrix, secondMatrix, THREAD_COUNT);
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
        if (!validate(firstMatrix, secondMatrix)) {
            return null;
        }

        int rows = firstMatrix.length;
        int cols = secondMatrix[0].length;
        int shared = firstMatrix[0].length;

        double[][] result = new double[rows][cols];

        if (threadCount > rows) {
            threadCount = rows; 
        }

        Thread[] threads = new Thread[threadCount];
        int chunkSize = rows / threadCount;
        int remainder = rows % threadCount;

        int startRow = 0;
        for (int t = 0; t < threadCount; t++) {
            int endRow = startRow + chunkSize + (t < remainder ? 1 : 0);
            final int rStart = startRow;
            final int rEnd = endRow;

            threads[t] = new Thread(() -> {
                for (int k = 0; k < shared; k++) {
                    double[] rowSecond = secondMatrix[k]; 
                    
                    for (int i = rStart; i < rEnd; i++) {
                        double a_ik = firstMatrix[i][k];
                        double[] rowResult = result[i];
                        for (int j = 0; j < cols; j++) {
                            rowResult[j] += a_ik * rowSecond[j];
                        }
                    }
                }
            });
            threads[t].start();
            startRow = endRow;
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    // Single threaded implementation for comparison (based on Lab 1 logic)
    public static double[][] multiplySingle(double[][] firstMatrix, double[][] secondMatrix) {
        if (!validate(firstMatrix, secondMatrix)) {
            return null;
        }

        int rows = firstMatrix.length;
        int cols = secondMatrix[0].length;
        int shared = firstMatrix[0].length;

        double[][] result = new double[rows][cols];

        for (int k = 0; k < shared; k++) {
            for (int i = 0; i < rows; i++) {
                double a_ik = firstMatrix[i][k];
                for (int j = 0; j < cols; j++) {
                    result[i][j] += a_ik * secondMatrix[k][j];
                }
            }
        }
        return result;
    }

    private static boolean validate(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null || firstMatrix.length == 0 || secondMatrix.length == 0) return false;
        if (firstMatrix[0].length != secondMatrix.length) return false;
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Processors available: " + Runtime.getRuntime().availableProcessors());
        
        int size = 1000; 
        System.out.println("Generating matrices of size " + size + "x" + size + "...");
        double[][] m1 = generateMatrix(size, size);
        double[][] m2 = generateMatrix(size, size);

        System.out.println("Starting Benchmark...");
        
        System.out.println("Warming up...");
        multiplySingle(new double[100][100], new double[100][100]);
        multiplyParallel(new double[100][100], new double[100][100], 2);

        long start = System.currentTimeMillis();
        multiplySingle(m1, m2);
        long end = System.currentTimeMillis();
        System.out.println("Single-threaded time: " + (end - start) + " ms");

        int[] threadCounts = {1, 2, 4, 8, 16, 32};
        
        for (int tc : threadCounts) {
            start = System.currentTimeMillis();
            multiplyParallel(m1, m2, tc);
            end = System.currentTimeMillis();
            System.out.println("Parallel time (" + tc + " threads): " + (end - start) + " ms");
        }
    }

    private static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }
}
