import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int n = firstMatrix.length;
        int a = firstMatrix[0].length;
        int b = secondMatrix[0].length;

        double[][] C = new double[n][b];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < b; j++) {
                double sum = 0.0;

                for (int k = 0; k < a; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }

                C[i][j] = sum;
            }
        }

        return C;
    }

    
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int threads = Runtime.getRuntime().availableProcessors();
        return multiplyParallel(firstMatrix, secondMatrix, threads);
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threads) {
        int n = firstMatrix.length;
        int a = firstMatrix[0].length;
        int b = secondMatrix[0].length;

        double[][] C = new double[n][b];
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < n; i++) {
            final int row = i;

            pool.execute(() -> {
                for (int j = 0; j < b; j++) {
                    double sum = 0.0;

                    for (int k = 0; k < a; k++) 
                        sum += firstMatrix[row][k] * secondMatrix[k][j];

                    C[row][j] = sum;
                }
            });
        }

        pool.shutdown();

        try {
            pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return C;
    }

    public static void main(String[] args) {
        int[] sizes = {10, 100, 500, 1000};
        int runs = 10;
        Random rand = new Random();

        System.out.printf( "%8s %12s %10s %10s %10s %10s %10s %10s %10s %8s%n", "Size", "Sequential", "2", "3", "4", "5", "6", "7", "8", "Best");

        for (int size : sizes) {
            double[][] A = new double[size][size];
            double[][] B = new double[size][size];

            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++) {
                    A[i][j] = rand.nextDouble();
                    B[i][j] = rand.nextDouble();
                }

            double seqTotal = 0.0;

            for (int r = 0; r < runs; r++) {
                long t1 = System.nanoTime();
                multiply(A, B);
                long t2 = System.nanoTime();
                seqTotal += (t2 - t1) / 1e9;
            }

            double seqAvg = seqTotal / runs;
            System.out.printf("%8d %12.6f", size, seqAvg);

            double bestTime = Double.MAX_VALUE;
            int bestThreads = 2;

            for (int threads = 2; threads <= 8; threads++) {
                double parTotal = 0.0;

                for (int r = 0; r < runs; r++) {
                    long t1 = System.nanoTime();
                    multiplyParallel(A, B, threads);
                    long t2 = System.nanoTime();
                    parTotal += (t2 - t1) / 1e9;
                }

                double parAvg = parTotal / runs;
                System.out.printf(" %10.6f", parAvg);

                if (parAvg < bestTime) {
                    bestTime = parAvg;
                    bestThreads = threads;
                }
            }

            System.out.printf(" %8d%n", bestThreads);
        }
    }
}