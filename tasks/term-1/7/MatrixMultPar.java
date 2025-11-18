import java.util.concurrent.*;

public class MatrixMultPar {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    private static final int PARALLEL_THRESHOLD = 500; // Изменен порог на 500

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix.length == 0 || secondMatrix.length == 0) {
            throw new IllegalArgumentException("Matrix can't be empty!");
        }

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;
        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Incompatible matrix!");
        }

        double[][] result = new double[rowsA][colsB];

        //используем многопоточность для матриц >= порога
        if (rowsA >= PARALLEL_THRESHOLD && colsB >= PARALLEL_THRESHOLD && AVAILABLE_PROCESSORS > 1) {
            //параллельная версия
            int nThreads = Math.min(AVAILABLE_PROCESSORS, rowsA);
            int rowsPerThread = (int) Math.ceil((double) rowsA / nThreads);

            try {
                Future<?>[] futures = new Future[nThreads];
                for (int i = 0; i < nThreads; i++) {
                    int startRow = i * rowsPerThread;
                    int endRow = Math.min(startRow + rowsPerThread, rowsA);
                    if (startRow < rowsA) {
                        futures[i] = executor.submit(new MatrixMultiplierTask(
                                firstMatrix, secondMatrix, result, startRow, endRow));
                    }
                }

                for (Future<?> future : futures) {
                    if (future != null) {
                        future.get();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Parallel multiply failed!", e);
            }
        } else {
            //однопоточная версия для маленьких матриц
            return multiply(firstMatrix, secondMatrix);
        }
        return result;
    }

    private static class MatrixMultiplierTask implements Runnable {
        private final double[][] A;
        private final double[][] B;
        private final double[][] C;
        private final int startRow;
        private final int endRow;

        public MatrixMultiplierTask(double[][] A, double[][] B, double[][] C, int startRow, int endRow) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run() {
            int n = A[0].length;
            int p = B[0].length;

            for (int i = startRow; i < endRow; i++) {
                double[] aRow = A[i];
                double[] cRow = C[i];
                for (int k = 0; k < n; k++) {
                    double aVal = aRow[k];
                    double[] bRow = B[k];
                    for (int j = 0; j < p; j++) {
                        cRow[j] += aVal * bRow[j];
                    }
                }
            }
        }
    }

    // однопоточная версия из лаб1
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int firstRows = firstMatrix.length;
        int firstColumns = firstMatrix[0].length;
        int secondColumns = secondMatrix[0].length;
        double[][] result = new double[firstRows][secondColumns];
        for (int i = 0; i < firstRows; i++) {
            for (int j = 0; j < firstColumns; j++) {
                for (int k = 0; k < secondColumns; k++) {
                    result[i][k] += firstMatrix[i][j] * secondMatrix[j][k];
                }
            }
        }
        return result;
    }

    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("AVAILABLE_PROCESSORS: " + AVAILABLE_PROCESSORS);
            System.out.println("PARALLEL_THRESHOLD: " + PARALLEL_THRESHOLD);

            int[] sizes = {500, 1000, 1500, 2000};
            int testRuns = 5;

            for (int size : sizes) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("Matrix size: " + size + "*" + size);

                double[][] matrix1 = generateRandomMatrix(size, size);
                double[][] matrix2 = generateRandomMatrix(size, size);

                long originalTime = testVersion("Original", matrix1, matrix2, testRuns,
                        MatrixMultPar::multiply);

                long parallelTime = testVersion("Parallel", matrix1, matrix2, testRuns,
                        MatrixMultPar::multiplyParallel);

                // Добавляем вычисление ускорения
                double speedup = (double) originalTime / parallelTime;
                System.out.printf("Speedup: %.2fx%n", speedup);
            }
        } finally {
            shutdown();
        }
    }

    private static long testVersion(String name, double[][] A, double[][] B, int runs,
                                    MatrixMultiplier multiplier) {
        long totalTime = 0;

        for (int i = 0; i < runs; i++) {
            System.gc();
            long startTime = System.nanoTime();
            multiplier.multiply(A, B);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;

            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        long avgTime = totalTime / runs;
        System.out.printf("%-20s - Time: %4d ms%n", name, avgTime);
        return avgTime;
    }

    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 10;
            }
        }
        return matrix;
    }

    @FunctionalInterface
    private interface MatrixMultiplier {
        double[][] multiply(double[][] A, double[][] B);
    }
}