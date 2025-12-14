public class MatrixMultPar {

    private static int coreCount = java.lang.Runtime.getRuntime().availableProcessors();

    private static final class ChunkWorker implements java.lang.Runnable {
        private final double[][] A;
        private final double[][] B;
        private final double[][] C;
        private final int rowStart;
        private final int rowEnd;

        ChunkWorker(double[][] mA, double[][] mB, double[][] mC, int s, int e) {
            A = mA;
            B = mB;
            C = mC;
            rowStart = s;
            rowEnd = e;
        }

        @Override
        public void run() {
            final int n = A[0].length;
            final int p = B[0].length;

            int i = rowStart;
            while (i < rowEnd) {
                int k = 0;
                while (k < n) {
                    double aik = A[i][k];
                    int j = 0;
                    while (j < p) {
                        C[i][j] += aik * B[k][j];
                        ++j;
                    }
                    ++k;
                }
                ++i;
            }
        }
    }

    private static double[][] makeZeroMatrix(int h, int w) {
        double[][] out = new double[h][w];
        for (int y = 0; y < h; ++y)
            java.util.Arrays.fill(out[y], 0.0);
        return out;
    }

    private static boolean areCompatible(double[][] X, double[][] Y) {
        if (X == null || Y == null) return false;
        if (X.length == 0 || Y.length == 0) return false;

        int colsX = X[0].length;
        int rowsY = Y.length;

        if (colsX != rowsY) {
            java.lang.System.out.println(
                "Error: Matrices can't be multiplied! " +
                "Number of columns in first matrix (" + colsX +
                ") must equal number of rows in second matrix (" + rowsY + ")"
            );
            return false;
        }
        return true;
    }

    public static void configureThreads(int desired) {
        if (desired < 1)
            throw new java.lang.IllegalArgumentException("Thread count must be ≥ 1");
        coreCount = desired;
    }

    public static int currentThreadLimit() {
        return coreCount;
    }

    public static double[][] compute(double[][] first, double[][] second) {
        if (!areCompatible(first, second))
            return null;

        int m = first.length;     // строки A
        int n = first[0].length;  // колонки A, строки B
        int p = second[0].length; // колонки B

        double[][] product = makeZeroMatrix(m, p);
        java.lang.Thread[] pool = new java.lang.Thread[coreCount];

        int chunkSize = (m + coreCount - 1) / coreCount;

        for (int idx = 0; idx < coreCount; ++idx) {
            int begin = idx * chunkSize;
            int finish = java.lang.Math.min(begin + chunkSize, m);

            if (begin >= m) break;

            java.lang.Runnable task = new ChunkWorker(first, second, product, begin, finish);
            pool[idx] = new java.lang.Thread(task);
            pool[idx].start();
        }

        for (int t = 0; t < coreCount; ++t) {
            if (pool[t] == null) continue;
            try {
                pool[t].join();
            } catch (java.lang.InterruptedException ex) {
                java.lang.System.out.println("Thread execution interrupted: " + ex.getMessage());
                java.lang.Thread.currentThread().interrupt();
                return null;
            }
        }

        return product;
    }

    public static double[][] genRandom(int height, int width, double lo, double hi) {
        double[][] mat = new double[height][width];
        for (int r = 0; r < height; ++r)
            for (int c = 0; c < width; ++c)
                mat[r][c] = java.lang.Math.random() * (hi - lo) + lo;
        return mat;
    }

    // оптимальное количество потоков
    public static void tune(double[][] X, double[][] Y, int trials) {
        int maxCores = java.lang.Runtime.getRuntime().availableProcessors();
        long fastest = java.lang.Long.MAX_VALUE;
        int best = 1;

        java.lang.System.out.println("\nFinding the optimal number of threads (from 1 to " + maxCores + "):");

        for (int thr = 1; thr <= maxCores; ++thr) {
            configureThreads(thr);
            long sum = 0L;

            for (int rep = 0; rep < trials; ++rep) {
                long t0 = java.lang.System.currentTimeMillis();
                compute(X, Y);
                long t1 = java.lang.System.currentTimeMillis();
                sum += (t1 - t0);
            }

            long avg = sum / trials;
            java.lang.System.out.println("\tThreads count: " + thr + ": " + avg + " ms");

            if (avg < fastest) {
                fastest = avg;
                best = thr;
            }
        }

        java.lang.System.out.println("\nResults of finding optimal:");
        java.lang.System.out.println("\tOptimal thread count: " + best);
        java.lang.System.out.println("\tAverage time with this number of threads: " + fastest + " ms");
    }

    public static void main(java.lang.String[] args) {
        final int size = 500;
        final int runs = 10;

        double[][] M1 = genRandom(size, size, 1.0, 100.0);
        double[][] M2 = genRandom(size, size, 1.0, 100.0);

        long total = 0L;

        java.lang.System.out.println("Launch on all possible threads:");
        for (int iter = 0; iter < runs; ++iter) {
            long ts = java.lang.System.currentTimeMillis();
            double[][] res = compute(M1, M2);
            long te = java.lang.System.currentTimeMillis();
            long dt = te - ts;

            total += dt;
            java.lang.System.out.println("\t#" + (iter + 1) + ": " + dt + " ms");
        }

        long mean = total / runs;
        java.lang.System.out.println("\nResults:");
        java.lang.System.out.println("\tAverage time: " + mean + " ms");
        tune(M1, M2, runs);
    }
}