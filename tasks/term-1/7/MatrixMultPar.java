import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMult {

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("incorrect size");
        }

        double[][] result = new double[rowsA][colsB];
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);


        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            executor.submit(() -> {
                for (int j = 0; j < colsB; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < colsA; k++) {
                        sum += firstMatrix[row][k] * secondMatrix[k][j];
                    }
                    result[row][j] = sum;
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return result;
    }

   
    public static double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        int size = 2000; 
        System.out.println("creating matrix" + size + "x" + size + "...");

        double[][] A = createRandomMatrix(size, size);
        double[][] B = createRandomMatrix(size, size);

        long startTime = System.nanoTime();
        double[][] C = multiplyParallel(A, B);
        long endTime = System.nanoTime();

        double duration = (endTime - startTime) / 1_000_000.0; 
        System.out.printf("time: %.2f%n", duration);
    }
}
