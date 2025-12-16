import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int PARALLEL_THRESHOLD = 130;

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

       // Если матрицы маленькие или только 1 процессор - используем однопоточную версию
//        if (rowsA < PARALLEL_THRESHOLD || colsB < PARALLEL_THRESHOLD || AVAILABLE_PROCESSORS == 1) {
//            return multiply(firstMatrix, secondMatrix);
//        }

        int nThreads = Math.min(AVAILABLE_PROCESSORS, rowsA);
        int rowsPerThread = (int) Math.ceil((double) rowsA / nThreads);

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        try {
            for (int i = 0; i < nThreads; i++) {
                final int startRow = i * rowsPerThread;
                final int endRow = Math.min(startRow + rowsPerThread, rowsA);

                if (startRow >= rowsA) break;
                executor.execute(() -> {
                    int n = firstMatrix[0].length; // cols in A = rows in B
                    int p = secondMatrix[0].length;

                    // оптимизация циклов i -> k -> j
                    for (int iRow = startRow; iRow < endRow; iRow++) {
                        double[] aRow = firstMatrix[iRow];
                        double[] cRow = result[iRow];
                        for (int k = 0; k < n; k++) {
                            double aVal = aRow[k];
                            double[] bRow = secondMatrix[k];
                            for (int j = 0; j < p; j++) {
                                cRow[j] += aVal * bRow[j];
                            }
                        }
                    }
                });
            }
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel multiply interrupted!", e);
            }
        }

        return result;
    }

    // однопоточная версия
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int firstRows = firstMatrix.length;
        int firstColumns = firstMatrix[0].length;
        int secondColumns = secondMatrix[0].length;
        double[][] result = new double[firstRows][secondColumns];

        //оптимизируем порядок циклов как в параллельной версии
        for (int i = 0; i < firstRows; i++) {
            double[] aRow = firstMatrix[i];
            double[] cRow = result[i];
            for (int k = 0; k < firstColumns; k++) {
                double aVal = aRow[k];
                double[] bRow = secondMatrix[k];
                for (int j = 0; j < secondColumns; j++) {
                    cRow[j] += aVal * bRow[j];
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("AVAILABLE_PROCESSORS: " + AVAILABLE_PROCESSORS);
        System.out.println("PARALLEL_THRESHOLD: " + PARALLEL_THRESHOLD);
        int[] sizes = {10, 100, 130, 150, 160, 200, 500, 1000, 1500};
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

            double speedup = (double) originalTime / parallelTime;
            System.out.printf("Speedup: %.2fx%n", speedup);
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