import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}
		if (firstMatrix.length == 0 || firstMatrix[0].length == 0) {
			throw new IllegalArgumentException("Первая матрица не может быть пустой");
		}
		if (secondMatrix.length == 0 || secondMatrix[0].length == 0) {
			throw new IllegalArgumentException("Вторая матрица не может быть пустой");
		}

		int m = firstMatrix.length;
		int n = firstMatrix[0].length;
		int p = secondMatrix[0].length;

		if (n != secondMatrix.length) {
			throw new IllegalArgumentException("Несовместимые размеры матриц");
		}

		double[][] result = new double[m][p];

		int numThreads = 16;
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		long startTime = System.nanoTime();

		int rowsPerThread = m / numThreads;
		for (int t = 0; t < numThreads; t++) {
			final int startRow = t * rowsPerThread;
			final int endRow = (t == numThreads - 1) ? m : (t + 1) * rowsPerThread;

			executor.submit(() -> {
				for (int i = startRow; i < endRow; i++) {
					for (int j = 0; j < p; j++) {
						double sum = 0.0;
						for (int k = 0; k < n; k++) {
							sum += firstMatrix[i][k] * secondMatrix[k][j];
						}
						result[i][j] = sum;
					}
				}
			});
		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Выполнение прервано", e);
		}

		long endTime = System.nanoTime();
		System.out.println("Время выполнения (параллельный): " + (endTime - startTime) / 1_000_000.0 + " мс");

		return result;
	}

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}
		if (firstMatrix.length == 0 || firstMatrix[0].length == 0) {
			throw new IllegalArgumentException("Первая матрица не может быть пустой");
		}
		if (secondMatrix.length == 0 || secondMatrix[0].length == 0) {
			throw new IllegalArgumentException("Вторая матрица не может быть пустой");
		}

		int m = firstMatrix.length;
		int n = firstMatrix[0].length;
		int p = secondMatrix[0].length;

		if (n != secondMatrix.length) {
			throw new IllegalArgumentException("Несовместимые размеры матриц");
		}

		double[][] result = new double[m][p];
		long startTime = System.nanoTime();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < p; j++) {
				for (int k = 0; k < n; k++) {
					result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
				}
			}
		}

		long endTime = System.nanoTime();
		System.out.println("Время выполнения (однопоточный): " + (endTime - startTime) / 1_000_000.0 + " мс");
		return result;
	}

	public static double[][] multiplyOptimizedUnrolled(double[][] firstMatrix, double[][] secondMatrix) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}
		if (firstMatrix.length == 0 || firstMatrix[0].length == 0) {
			throw new IllegalArgumentException("Первая матрица не может быть пустой");
		}
		if (secondMatrix.length == 0 || secondMatrix[0].length == 0) {
			throw new IllegalArgumentException("Вторая матрица не может быть пустой");
		}

		int m = firstMatrix.length;
		int n = firstMatrix[0].length;
		int p = secondMatrix[0].length;

		if (n != secondMatrix.length) {
			throw new IllegalArgumentException("Несовместимые размеры матриц");
		}

		double[][] result = new double[m][p];
		long startTime = System.nanoTime();

		final int UNROLL_FACTOR = 4;

		for (int i = 0; i < m; i++) {
			for (int k = 0; k < n; k++) {
				double temp = firstMatrix[i][k];
				double[] resultRow = result[i];
				double[] secondRow = secondMatrix[k];

				int j = 0;
				for (; j <= p - UNROLL_FACTOR; j += UNROLL_FACTOR) {
					resultRow[j] += temp * secondRow[j];
					resultRow[j + 1] += temp * secondRow[j + 1];
					resultRow[j + 2] += temp * secondRow[j + 2];
					resultRow[j + 3] += temp * secondRow[j + 3];
				}
				for (; j < p; j++) {
					resultRow[j] += temp * secondRow[j];
				}
			}
		}

		long endTime = System.nanoTime();
		System.out.println("Время выполнения (оптимизированный): " + (endTime - startTime) / 1_000_000.0 + " мс");
		return result;
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

	public static void main(String[] args) {
		System.out.println("=== Тестирование параллельного умножения матриц ===");
		int size = 2000;
		System.out.println("Размер матриц: " + size + "x" + size);

		double[][] A = generateRandomMatrix(size, size);
		double[][] B = generateRandomMatrix(size, size);

		System.out.println("\nИзмерение времени выполнения:");

		multiply(A, B);

		multiplyOptimizedUnrolled(A, B);

		multiplyParallel(A, B);
	}
}