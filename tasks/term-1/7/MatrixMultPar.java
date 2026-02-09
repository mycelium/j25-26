import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MatrixMultPar {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
		int rowsFirstMatrix = firstMatrix.length;
		int colsFirstMatrix = firstMatrix[0].length;
		int rowsSecondMatrix = secondMatrix.length;
		int colsSecondMatrix = secondMatrix[0].length;

		if (colsFirstMatrix != rowsSecondMatrix) {
			throw new IllegalArgumentException("Matrix dimensions do not match for multiplication");
		}

		double[][] resultMatrix = new double[rowsFirstMatrix][colsSecondMatrix];

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		List<Future<Void>> futures = new ArrayList<>();

		for (int i = 0; i < rowsFirstMatrix; i++) {
			final int row = i;
			futures.add(executor.submit(() -> {
				for (int j = 0; j < colsSecondMatrix; j++) {
					for (int k = 0; k < colsFirstMatrix; k++) {
						resultMatrix[row][j] += firstMatrix[row][k] * secondMatrix[k][j];
					}
				}
				return null;
			}));
		}

		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();
		return resultMatrix;
	}

	public static void main(String[] args) {
		int size = 1000;

		double[][] firstMatrix = new double[size][size];
		double[][] secondMatrix = new double[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				firstMatrix[i][j] = Math.random();
				secondMatrix[i][j] = Math.random();
			}
		}


		int p = Runtime.getRuntime().availableProcessors();
		System.out.println("Count of processors: " + p );

		int[] numsThreads = {p-2, p-1, p, p+1, p+2, size};

		for (int numThreads: numsThreads){
			System.out.println("for " + numThreads + " threads:");
			long startTime = System.currentTimeMillis();
			double[][] resultParallel = multiplyParallel(firstMatrix, secondMatrix, numThreads);
			long endTime = System.currentTimeMillis();
			System.out.println("\tParallel execution time: " + (endTime - startTime) + " ms");
		}
		long startTime = System.currentTimeMillis();
		double[][] resultMultiply = MatrixMult.multiply(firstMatrix, secondMatrix);
		long endTime = System.currentTimeMillis();
		System.out.println("\nMultiply execution time: " + (endTime - startTime) + " ms");

		startTime = System.currentTimeMillis();
		double[][] resultMultiplyOpt = MatrixMult.multiply(firstMatrix, secondMatrix);
		endTime = System.currentTimeMillis();
		System.out.println("MultiplyOpt execution time: " + (endTime - startTime) + " ms");
	}
}
