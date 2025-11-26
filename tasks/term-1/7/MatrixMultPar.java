import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

public class MatrixMultPar {


    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int p = firstMatrix[0].length;
        int p2 = secondMatrix.length;
        int n = secondMatrix[0].length;
        if (p != p2) throw new IllegalArgumentException("Wrong sizes");
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
        if (p != p2) throw new IllegalArgumentException("Wrong sizes");

        double[][] res = new double[m][n];

        double[][] secondT = new double[n][p];
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < n; j++) {
                secondT[j][i] = secondMatrix[i][j];
            }
        }

        int threadsCount = Math.max(1, numThreads);
        List<Thread> myThreads = new ArrayList<>();
        int rowsPerTask = Math.max(1, (m + threadsCount - 1) / threadsCount);

        for (int start = 0; start < m; start += rowsPerTask) {
            final int iStart = start;
            final int iEnd = Math.min(m, start + rowsPerTask);


            Runnable workerTask = new Runnable() {
                @Override
                public void run() {
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
                }
            };


            Thread t = new Thread(workerTask);
            t.setName("Manual-Worker-" + start);


            t.start();
            myThreads.add(t);
        }


        for (Thread t : myThreads) {
            t.join();
        }

        return res;
    }

    public static double[][] randomMatrix(int rows, int cols, long seed) {
        Random rnd = new Random(seed);
        double[][] a = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a[i][j] = rnd.nextDouble();
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

        // --- прогрев  ---


        int small = 64;
        double[][] wa = randomMatrix(small, small, baseSeed + 1);
        double[][] wb = randomMatrix(small, small, baseSeed + 2);
        for (int i = 0; i < 5; i++) {
            multiply(wa, wb);
            multiplyParallel(wa, wb, available);
        }



        for (int size : sizes) {

            double[][] A = randomMatrix(size, size, baseSeed + size);
            double[][] B = randomMatrix(size, size, baseSeed + size + 1000);

            // 1. Тест 1 потока
            double totalSingleMs = 0.0;
            for (int a = 0; a < attempts; a++) {
                long t0 = System.nanoTime();
                multiply(A, B);
                long t1 = System.nanoTime();
                totalSingleMs += (t1 - t0) / 1_000_000.0;


                System.gc();
                Thread.sleep(10);
            }
            double avgSingleMs = totalSingleMs / attempts;
            System.out.printf("Size %d -> single run: avg = %.3f ms%n", size, avgSingleMs);

            // 2. Тест параллельного выполнения
            for (int threads : threadCandidates) {
                double totalParMs = 0.0;
                for (int a = 0; a < attempts; a++) {
                    long t0 = System.nanoTime();


                    multiplyParallel(A, B, threads);

                    long t1 = System.nanoTime();
                    totalParMs += (t1 - t0) / 1_000_000.0;

                    System.gc();
                    Thread.sleep(10);
                }
                double avgParMs = totalParMs / attempts;


                double speedup = avgSingleMs / avgParMs;
                System.out.printf("Size %d -> parallel (%d threads): avg = %.3f ms (Speedup: %.2fx)%n",
                        size, threads, avgParMs, speedup);
            }
            System.out.println("--------------------------------------------------");
        }
    }
}