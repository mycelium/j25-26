import java.util.Random;
import java.util.concurrent.*;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

public class MatrixMultPar{


    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int p = firstMatrix[0].length;
        int p2 = secondMatrix.length;
        int n = secondMatrix[0].length;
        if (p != p2) {
            throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
        }
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < p; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                res[i][j] = sum;
            }
        }
        return res;
    }


    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) throws InterruptedException {
        int m = firstMatrix.length;
        int p = firstMatrix[0].length;
        int p2 = secondMatrix.length;
        int n = secondMatrix[0].length;
        if (p != p2) {
            throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
        }

        double[][] res = new double[m][n];


        double[][] secondT = new double[n][p];
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < n; j++) {
                secondT[j][i] = secondMatrix[i][j];
            }
        }

        int threads = Math.max(1, numThreads);
        ExecutorService ex = Executors.newFixedThreadPool(threads);

        int rowsPerTask = Math.max(1, (m + threads - 1) / threads);
        int tasks = (m + rowsPerTask - 1) / rowsPerTask;
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int start = 0; start < m; start += rowsPerTask) {
            final int iStart = start;
            final int iEnd = Math.min(m, start + rowsPerTask);
            ex.submit(() -> {
                for (int i = iStart; i < iEnd; i++) {
                    double[] rowA = firstMatrix[i];
                    double[] rowRes = res[i];
                    for (int j = 0; j < n; j++) {
                        double sum = 0.0;
                        double[] colB = secondT[j];
                        for (int k = 0; k < p; k++) {
                            sum += rowA[k] * colB[k];
                        }
                        rowRes[j] = sum;
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        ex.shutdown();
        return res;
    }



    public static double[][] randomMatrix(int rows, int cols, long seed) {
        Random rnd = new Random(seed);
        double[][] a = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            double[] row = a[i];
            for (int j = 0; j < cols; j++) {
                row[j] = rnd.nextDouble();
            }
        }
        return a;
    }


    public static void main(String[] args) throws Exception {
        int available = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors (logical): " + available);


        int[] sizes = new int[] {200, 500, 1000};
        final int attempts = 10;
        long baseSeed = 12345L;


        int half = Math.max(1, available / 2);
        int[] threadCandidates = new int[] {2, half, available};
        threadCandidates = Arrays.stream(threadCandidates).distinct().filter(x -> x > 0).toArray();


        int small = 64;
        double[][] wa = randomMatrix(small, small, baseSeed + 1);
        double[][] wb = randomMatrix(small, small, baseSeed + 2);
        for (int i = 0; i < 3; i++) {
            multiply(wa, wb);
            multiplyParallel(wa, wb, Math.max(1, available));
        }

        for (int size : sizes) {

            double[][] A = randomMatrix(size, size, baseSeed + size);
            double[][] B = randomMatrix(size, size, baseSeed + size + 1000);

            double totalSingleMs = 0.0;
            for (int a = 0; a < attempts; a++) {
                long t0 = System.nanoTime();
                double[][] single = multiply(A, B);
                long t1 = System.nanoTime();
                totalSingleMs += (t1 - t0) / 1_000_000.0;
                System.gc();
                Thread.sleep(20);
            }
            double avgSingleMs = totalSingleMs / attempts;
            System.out.printf("Size %d -> single run: avg = %.3f ms%n", size, avgSingleMs);


            double[][] singleRef = multiply(A, B);


            Map<Integer, Double> avgMap = new LinkedHashMap<>();
            for (int threads : threadCandidates) {
                double totalParMs = 0.0;
                boolean correctnessChecked = false;
                for (int a = 0; a < attempts; a++) {
                    long t0 = System.nanoTime();
                    double[][] par = multiplyParallel(A, B, threads);
                    long t1 = System.nanoTime();
                    totalParMs += (t1 - t0) / 1_000_000.0;






                    System.gc();
                    Thread.sleep(10);
                }
                double avgParMs = totalParMs / attempts;
                avgMap.put(threads, avgParMs);
                System.out.printf("Size %d -> parallel run (%d threads): avg = %.3f ms%n", size, threads, avgParMs);
            }


        }
    }
}
