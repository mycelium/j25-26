import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {
    static class MatrixMultiplierTask implements Runnable {
        private final double[][] firstMatrix;
        private final double[][] secondMatrix;
        private final double[][] resultMatrix;
        private final int startRow;
        private final int endRow;
        public MatrixMultiplierTask(double[][] firstMatrix, double[][] secondMatrix,
                                    double[][] resultMatrix, int startRow, int endRow) {
            this.firstMatrix = firstMatrix;
            this.secondMatrix = secondMatrix;
            this.resultMatrix = resultMatrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }
        @Override
        public void run() {
            int colsFirst = firstMatrix[0].length;
            int colsSecond = secondMatrix[0].length;
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < colsFirst; j++) {
                    double firstMatrixIJ = firstMatrix[i][j];
                    for (int k = 0; k < colsSecond; k++) {
                        resultMatrix[i][k] += firstMatrixIJ * secondMatrix[j][k];
                    }
                }
            }
        }
    }
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsFirst = firstMatrix.length;
        int colsFirst = firstMatrix[0].length;
        int rowsSecond = secondMatrix.length;
        int colsSecond = secondMatrix[0].length;
        if (colsFirst != rowsSecond) {
            throw new IllegalArgumentException("Невозможно умножить матрицы.");
        }
        double[][] result = new double[rowsFirst][colsSecond];
        int optimalThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(optimalThreads);
        int rowsPerThread = rowsFirst / optimalThreads;
        int extraRows = rowsFirst % optimalThreads;
        int currentRow = 0;
        for (int i = 0; i < optimalThreads; i++) {
            int rowsToProcess = rowsPerThread + (i < extraRows ? 1 : 0);
            if (rowsToProcess > 0) {
                int endRow = currentRow + rowsToProcess;
                executor.execute(new MatrixMultiplierTask(firstMatrix, secondMatrix, result, currentRow, endRow));
                currentRow = endRow;
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Таймаут при выполнении умножения матриц");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Умножение матриц было прервано", e);
        }
        return result;
    }
    public static double[][] generateMatrix(int rows, int cols) {
        Random random = new Random();
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble() * 10.0;
            }
        }
        return matrix;
    }
    public static void runTests() {
        int[][] testSizes = {
                {500, 500, 500},
                {800, 800, 800},
                {1000, 1000, 1000},
                {1500, 1500, 1500}};
        int iterations = 10;
        for (int[] sizes : testSizes) {
            int rows1 = sizes[0];
            int cols1 = sizes[1];
            int cols2 = sizes[2];
            System.out.printf("Умножение матриц %dx%d, ", rows1, cols1);
            double[][] firstMatrix = generateMatrix(rows1, cols1);
            double[][] secondMatrix = generateMatrix(cols1, cols2);
            long totalTime = 0;
            for (int i = 0; i < iterations; i++) {
                long startTime = System.currentTimeMillis();
                multiplyParallel(firstMatrix, secondMatrix);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            double avgTime = (double) totalTime / iterations;
            System.out.printf(" среднее время = %.2f мс\n", avgTime);
        }
    }
    public static void findOptimalThreads(int size) {
        double[][] firstMatrix = generateMatrix(size, size);
        double[][] secondMatrix = generateMatrix(size, size);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.printf("Поиск оптимального количества потоков для матрицы %dx%d\n", size, size);
        for (int threads = 4; threads <= availableProcessors; threads += 4) {
            final int testThreads = threads;
            long startTime = System.currentTimeMillis();
            multiplyParallelT(firstMatrix, secondMatrix, testThreads);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Время выполнения с " + threads + " потоками: " + duration + " мс");
        }
        System.out.printf("\n");
    }
    private static double[][] multiplyParallelT(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
        int rowsFirst = firstMatrix.length;
        int colsFirst = firstMatrix[0].length;
        int rowsSecond = secondMatrix.length;
        int colsSecond = secondMatrix[0].length;
        if (colsFirst != rowsSecond) {
            throw new IllegalArgumentException("Невозможно умножить матрицы");
        }
        double[][] result = new double[rowsFirst][colsSecond];
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        int rowsPerThread = rowsFirst / threadCount;
        int extraRows = rowsFirst % threadCount;
        int currentRow = 0;
        for (int i = 0; i < threadCount; i++) {
            int rowsToProcess = rowsPerThread + (i < extraRows ? 1 : 0);
            if (rowsToProcess > 0) {
                int endRow = currentRow + rowsToProcess;
                executor.execute(new MatrixMultiplierTask(firstMatrix, secondMatrix, result, currentRow, endRow));
                currentRow = endRow;
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Таймаут при выполнении умножения матриц");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Умножение матриц было прервано", e);
        }
        return result;
    }
    public static void main(String[] args) {
        runTests();
        findOptimalThreads(500);
        findOptimalThreads(1000);
        findOptimalThreads(1500);
        findOptimalThreads(2000);
    }
}