import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MatrixMultPar {

	private static int lastUsedThreads = 1;
	private static final Map<Integer, Integer> optimalThreadsCache = new HashMap<>();
	private static final int REPETITIONS = 3;
	private static final Map<Integer, ExecutorService> executorsByThreadCount = new HashMap<>();

	public static double[][] multiplyParallel(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;
        if (colsA != rowsB) {
        		throw new IllegalArgumentException("Matrices cannot be multiplied: " + "number of columns in the first matrix (" + colsA + ") " +
                                           "does not equal the number of rows in the second matrix (" + rowsB + ")");
        }
        double[][] B_T = new double[colsB][rowsB];
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                B_T[j][i] = B[i][j];
            }
        }
		int numThreads;
		if (rowsA < 2000){
			numThreads = optimalThreadsCache.computeIfAbsent(rowsA, k -> calculateOptimalThreads(rowsA));
		}
		else {
			numThreads = Runtime.getRuntime().availableProcessors();
		}
		lastUsedThreads = numThreads;
        return multiplyParallel(A, B_T, numThreads);
    }
	
	private static double[][] multiplyParallel(double[][] A, double[][] B, int numThreads) {
		double[][] result = new double[A.length][B[0].length];
		ExecutorService executor = executorsByThreadCount.computeIfAbsent(
            numThreads,
            n -> Executors.newFixedThreadPool(n)
        );
        int rowsPerThread = (int) Math.ceil((double) A.length / numThreads);
        List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, A.length);

            futures.add(executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < B.length; j++) {
                        double sum = 0;
                        for (int k = 0; k < A[0].length; k++) {
                            sum += A[i][k] * B[j][k];
                        }
                        result[i][j] = sum;
                    }
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static double[][] createMatrix(int row, int col, int min, int max){
        double[][] matrix = new double[row][col];
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                matrix[i][j] = Math.random() * (max - min + 1) + min;
            }
        }
        return matrix;
    }

	private static int calculateOptimalThreads(int size) {
        int available = Runtime.getRuntime().availableProcessors();
        double[][] m1 = createMatrix(size, size, 0, 10);
        double[][] m2 = createMatrix(size, size, 0, 10);

        double bestTime = Double.MAX_VALUE;
        int bestThreads = 1;

        System.out.printf("Testing threads for %dx%d matrix:%n", size, size);

        for (int threads = 1; threads <= available * 2; threads++) {
            long totalTime = 0;

            for (int warmup = 0; warmup < 2; warmup++) {
                multiplyParallel(m1, m2, threads);
            }
            for (int i = 0; i < REPETITIONS; i++) {
                long start = System.nanoTime();
                multiplyParallel(m1, m2, threads);
                long end = System.nanoTime();
                totalTime += (end - start);
            }

            double avgTime = totalTime / (double) REPETITIONS / 1_000_000.0;
            if (avgTime < bestTime) {
                bestTime = avgTime;
                bestThreads = threads;
            }
        }

        System.out.printf("Best for %dx%d: %d threads (%.2f ms)%n%n",
            size, size, bestThreads, bestTime);
        return bestThreads;
    }

	public static void benchmark(int size, int repetitions) {
        System.out.println("Testing size: " + size + "x" + size);

        double[][] m1 = createMatrix(size, size, 0, 10);
        double[][] m2 = createMatrix(size, size, 0, 10);

        for (int i = 0; i < 2; i++) {
            multiplyParallel(m1, m2);
        }
        long totalTime = 0;
        for (int i = 0; i < repetitions; i++) {
            long start = System.nanoTime();
            multiplyParallel(m1, m2);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        double avg = totalTime / (double) repetitions / 1_000_000.0;
        System.out.printf("Average time (%d threads): %.3f ms%n%n", lastUsedThreads, avg);
    }

    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000, 2000};

        try {
            for (int size : sizes) {
                benchmark(size, 5);
            }
        } finally {
            for (ExecutorService executor : executorsByThreadCount.values()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        }
    }

}
