import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMult {

    private static final int BLOCK_SIZE = 128;

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];
        long operations = (long) rowsA * colsA * colsB;

        if (operations > 50_000_000L) {
            for (int i0 = 0; i0 < rowsA; i0 += BLOCK_SIZE) {
                int i1 = Math.min(i0 + BLOCK_SIZE, rowsA);
                for (int k0 = 0; k0 < colsA; k0 += BLOCK_SIZE) {
                    int k1 = Math.min(k0 + BLOCK_SIZE, colsA);
                    for (int j0 = 0; j0 < colsB; j0 += BLOCK_SIZE) {
                        int j1 = Math.min(j0 + BLOCK_SIZE, colsB);
                        for (int i = i0; i < i1; i++) {
                            for (int k = k0; k < k1; k++) {
                                double a = firstMatrix[i][k];
                                for (int j = j0; j < j1; j++) {
                                    result[i][j] += a * secondMatrix[k][j];
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < rowsA; i++) {
                for (int k = 0; k < colsA; k++) {
                    double a = firstMatrix[i][k];
                    for (int j = 0; j < colsB; j++) {
                        result[i][j] += a * secondMatrix[k][j];
                    }
                }
            }
        }
        return result;
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int rowsPerThread = Math.max(1, rowsA / numThreads);
        
        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? rowsA : startRow + rowsPerThread;
            
            executor.execute(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int k = 0; k < colsA; k++) {
                        double a = firstMatrix[i][k];
                        for (int j = 0; j < colsB; j++) {
                            result[i][j] += a * secondMatrix[k][j];
                        }
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000};
        int iterations = 5;
        Random rand = new Random(0);

        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Матрица " + size + "x" + size);
            System.out.println("=".repeat(50));

            double[][] A = new double[size][size];
            double[][] B = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    A[i][j] = rand.nextDouble() * 10;
                    B[i][j] = rand.nextDouble() * 10;
                }
            }

            long totalTimeSingle = 0;
            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();
                multiply(A, B);
                long end = System.currentTimeMillis();
                totalTimeSingle += (end - start);
            }
            double avgTimeSingle = (double) totalTimeSingle / iterations;
            System.out.printf("Однопоточное: %.2f мс\n", avgTimeSingle);

            for (int threads : new int[]{1, 2, 4}) {
                long totalTimeParallel = 0;
                for (int i = 0; i < iterations; i++) {
                    long start = System.currentTimeMillis();
                    multiplyParallel(A, B, threads);
                    long end = System.currentTimeMillis();
                    totalTimeParallel += (end - start);
                }
                double avgTimeParallel = (double) totalTimeParallel / iterations;
                double speedup = avgTimeSingle / avgTimeParallel;
                System.out.printf("%d потоков: %.2f мс, ускорение: %.2fx\n", 
                    threads, avgTimeParallel, speedup);
            }
        }
    }
}
