import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

	public static double[][] createMatrix(int rows, int cols, double minValue, double maxValue) {
		double[][] matrix = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random() * (maxValue - minValue + 1) + minValue;
			}
		}
		return matrix;
	}

	public static void printMatrix(double[][] matrix) {
		try {
			if (matrix == null) {
				throw new IllegalArgumentException("Null matrix can't be printed!");
			}

			System.out.print("Matrix:\n");
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					System.out.printf("%.4f\t", matrix[i][j]);
				}
				System.out.print("\n");
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}

	public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
		double[][] result = new double[0][0];
		try {
			if (firstMatrix == null || secondMatrix == null) {
				throw new IllegalArgumentException("The matrix being multiplied cannot be null!");
			}

			int rowsFirstMatrix = firstMatrix.length;
			int colsFirstMatrix = firstMatrix[0].length;
			int rowsSecondMatrix = secondMatrix.length;
			int colsSecondMatrix = secondMatrix[0].length;

			if (colsFirstMatrix != rowsSecondMatrix) {
				throw new IllegalArgumentException("Matrix can't be multiplied!");
			}

			double[][] resultMatrix = new double[rowsFirstMatrix][colsSecondMatrix];

			for (int i = 0; i < rowsFirstMatrix; i++) {
				for (int k = 0; k < colsFirstMatrix; k++) {
					double value = firstMatrix[i][k];
					for (int j = 0; j < colsSecondMatrix; j++) {
						resultMatrix[i][j] += value * secondMatrix[k][j];
					}
				}
			}
			result = resultMatrix;

		} catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
		}
		return result;
	}

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadCount) {
		try {
			if (firstMatrix == null || secondMatrix == null) {
				throw new IllegalArgumentException("The matrix being multiplied cannot be null!");
			}

			int rowsA = firstMatrix.length;
			int colsA = firstMatrix[0].length;
			int rowsB = secondMatrix.length;
			int colsB = secondMatrix[0].length;

			if (colsA != rowsB) {
				throw new IllegalArgumentException("Matrix can't be multiplied!");
			}

			double[][] result = new double[rowsA][colsB];

			int rowsPerThread = rowsA / threadCount;
			int extraRows = rowsA % threadCount;

			List<Thread> threads = new ArrayList<>();

			int startRow = 0;
			for (int t = 0; t < threadCount; t++) {
				int rowsForThisThread = rowsPerThread + (t < extraRows ? 1 : 0);
				int endRow = startRow + rowsForThisThread;

				final int finalStartRow = startRow;
				final int finalEndRow = endRow;

				Thread thread = new Thread(() -> {
					for (int i = finalStartRow; i < finalEndRow; i++) {
						for (int k = 0; k < colsA; k++) {
							double value = firstMatrix[i][k];
							for (int j = 0; j < colsB; j++) {
								result[i][j] += value * secondMatrix[k][j];
							}
						}
					}
				});

				threads.add(thread);
				thread.start();
				startRow = endRow;
			}

			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Thread interrupted", e);
				}
			}

			return result;

		} catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
			return new double[0][0];
		}
	}



	public static int findOptimalThreadCount(double[][] A, double[][] B, int maxThreads, int runs) {
		long bestTime = Long.MAX_VALUE;
		int bestThreads = 1;

		for (int threads = 1; threads <= maxThreads; threads++) {
			long totalTime = 0;
			for (int i = 0; i < runs; i++) {
				long start = System.currentTimeMillis();
				multiplyParallel(A, B, threads);
				long end = System.currentTimeMillis();
				totalTime += (end - start);
			}
			long avgTime = totalTime / runs;
			System.out.printf("Threads: %2d | Avg time: %5d ms%n", threads, avgTime);

			if (avgTime < bestTime) {
				bestTime = avgTime;
				bestThreads = threads;
			}
		}

		System.out.println("\nOptimal thread count: " + bestThreads + " (avg time: " + bestTime + " ms)");
		return bestThreads;
	}

	public static void main(String[] args) {
		int size = 1500;
		int runs = 10;

		double[][] matrix1 = createMatrix(size, size, 0, 100);
		double[][] matrix2 = createMatrix(size, size, 0, 100);

		System.out.println("\n=== TESTING THREAD COUNTS ===");
		int optimalThreads = findOptimalThreadCount(matrix1, matrix2, Runtime.getRuntime().availableProcessors(), runs);

		long totalSerial = 0;
		for (int i = 0; i < runs; i++) {
			long start = System.currentTimeMillis();
			multiplyOptimized(matrix1, matrix2);
			long end = System.currentTimeMillis();
			totalSerial += (end - start);
		}
		long avgSerial = totalSerial / runs;
		System.out.println("\n=== SERIAL MULTIPTY MATRICES ===");
		System.out.println("Average time: " + avgSerial + " ms");

		System.out.println("\n=== PARALLEL MULTIPTY MATRICES ===");

		long totalParallel = 0;
		for (int i = 0; i < runs; i++) {
			long start = System.currentTimeMillis();
			multiplyParallel(matrix1, matrix2, optimalThreads);
			long end = System.currentTimeMillis();
			totalParallel += (end - start);
		}
		long avgParallel = totalParallel / runs;
		System.out.println("\nOptimal threads: " + optimalThreads);
		System.out.println("Average time: " + avgParallel + " ms");
	}
}