
public class MatrixMultPar {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
		return multiplyParallel(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());
	}
	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Matrices cannot be null");
		}

		int aRows = firstMatrix.length;
		int aCols = firstMatrix[0].length;
		int bRows = secondMatrix.length;
		int bCols = secondMatrix[0].length;

		if (aCols != bRows) {
			throw new IllegalArgumentException("Incompatible matrix sizes: " +
				aCols + " != " + bRows);
		}

		double[][] result = new double[aRows][bCols];
		int rowsPerThread = aRows / numThreads;
		int extraRows = aRows % numThreads;
		Thread[] threads = new Thread[numThreads];
		int startRow = 0;

		for (int thread = 0; thread < numThreads; thread++) {
			int endRow = startRow + rowsPerThread + (thread < extraRows ? 1 : 0);
			final int threadStartRow = startRow;
			final int threadEndRow = endRow;
			Runnable task = () -> {
				for (int i = threadStartRow; i < threadEndRow; i++) {
					for (int k = 0; k < aCols; k++) {
						double aik = firstMatrix[i][k];
						for (int j = 0; j < bCols; j++) {
							result[i][j] += aik * secondMatrix[k][j];
						}
					}
				}
			};
			threads[thread] = new Thread(task);
			startRow = endRow;
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException("Error while executing parallel multiplication", e);
			}
		}
		return result;
	}
	public static double[][] multiplySequential(double[][] firstMatrix, double[][] secondMatrix) {
		int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;

        double[][] result = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < p; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < m; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }
        return result;
	}

	
	public static int findOptimalThreadCount(double[][] matrixA, double[][] matrixB) {
		int maxThreads = 16; 
		long bestTime = Long.MAX_VALUE;
		int optimalThreads = 1;

		System.out.println("Selecting the optimal number of threads...");
		System.out.println("Threads | Times (ms)| Speedup");
		System.out.println("--------|-----------|----------");

		long sequentialTime = measureTime(() -> multiplySequential(matrixA, matrixB), 10);
		System.out.printf("%7d | %9d | %.2fx%n", 1, sequentialTime, 1.0);

		for (int threads = 2; threads <= maxThreads; threads++) {
			final int numThreads = threads; 
			long time = measureTime(() -> multiplyParallel(matrixA, matrixB, numThreads), 10);
			double speedup = (double) sequentialTime / time;
			System.out.printf("%7d | %9d | %.2fx%n", threads, time, speedup);
			if (time < bestTime) {
				bestTime = time;
				optimalThreads = threads;
			}
		}
		System.out.println("Optimal number of threads: " + optimalThreads);
		return optimalThreads;
	}

	private static long measureTime(Runnable task, int iterations) {
		for (int i = 0; i < 2; i++) {
			task.run();
		}
		long totalTime = 0;
		for (int i = 0; i < iterations; i++) {
			long startTime = System.nanoTime();
			task.run();
			long endTime = System.nanoTime();
			totalTime += (endTime - startTime) / 1_000_000; 
		}
		return totalTime / iterations;
	}
	private static double[][] generateRandomMatrix(int rows, int cols) {
		double[][] matrix = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random() * 100;
			}
		}
		return matrix;
	}

	private static boolean matricesEqual(double[][] a, double[][] b, double tolerance) {
		if (a.length != b.length || a[0].length != b[0].length) {
			return false;
		}

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				if (Math.abs(a[i][j] - b[i][j]) > tolerance) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println("=======Parallel Matrix Multiplication=======\n");
		int[] sizes = {500, 1000, 1500};

		for (int size : sizes) {
			System.out.println("Testing matrices " + size + "x" + size);
			System.out.println("=".repeat(50));
			double[][] matrixA = generateRandomMatrix(size, size);
			double[][] matrixB = generateRandomMatrix(size, size);

			int optimalThreads = findOptimalThreadCount(matrixA, matrixB);

			System.out.println("\nPerformance comparison:");
			System.out.println("| Method             | Time (ms)  | Speedup |");
			System.out.println("|--------------------|------------|-----------|");

			long sequentialTime = measureTime(() -> multiplySequential(matrixA, matrixB), 10);
			System.out.printf("| Sequential         | %10d | %.2fx     |%n", sequentialTime, 1.0);

			long parallelTime = measureTime(() -> multiplyParallel(matrixA, matrixB, optimalThreads), 10);
			double speedup = (double) sequentialTime / parallelTime;
			System.out.printf("| Parallel (%2d)      | %10d | %.2fx     |%n",
				optimalThreads, parallelTime, speedup);

			double[][] seqResult = multiplySequential(matrixA, matrixB);
			double[][] parResult = multiplyParallel(matrixA, matrixB, optimalThreads);

			if (matricesEqual(seqResult, parResult, 1e-10)) {
				System.out.println("Results match!!");
			} else {
				System.out.println("Results differ!");
			}

			System.out.println();
		}

		System.out.println("System info:");
		System.out.println("Available CPU cores: " + Runtime.getRuntime().availableProcessors());
		System.out.println("JVM max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
	}
}