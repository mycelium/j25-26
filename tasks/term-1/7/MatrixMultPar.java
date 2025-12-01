public class MatrixMultPar {
	private static int numThreads = Runtime.getRuntime().availableProcessors();

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {

		int n = firstMatrix.length;
		int m = firstMatrix[0].length;
		int p = secondMatrix[0].length;

		if (firstMatrix == null || secondMatrix == null)
			throw new IllegalArgumentException("Матрица не может быть null");
		if (n == 0 || m == 0 ||
				secondMatrix.length == 0 || p == 0)
			throw new IllegalArgumentException("Матрица пустая");
		if (m != secondMatrix.length)
			throw new IllegalArgumentException("Несовместимые размеры матриц");

		double[][] result = new double[n][p];

		Thread[] threads = new Thread[numThreads];

		int rowsPerThread = (n + numThreads - 1) / numThreads;

		for (int t = 0; t < numThreads; t++) {

			final int startRow = t * rowsPerThread;
			final int endRow = Math.min(startRow + rowsPerThread, n);

			threads[t] = new Thread(() -> {
				for (int i = startRow; i < endRow; i++) {
					for (int k = 0; k < m; k++) {
						double firstVal = firstMatrix[i][k];
						for (int j = 0; j < p; j++) {
							result[i][j] += firstVal * secondMatrix[k][j];
						}
					}
				}
			});

			threads[t].start();
		}

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		return result;
	}

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрица не может быть null");
		}
		if (firstMatrix.length == 0 || firstMatrix[0].length == 0 || secondMatrix.length == 0
				|| secondMatrix[0].length == 0) {
			throw new IllegalArgumentException("Матрица пустая");
		}
		if (firstMatrix[0].length != secondMatrix.length) {
			throw new IllegalArgumentException("Несовместимые размеры матриц");
		}

		double[][] result = new double[firstMatrix.length][secondMatrix[0].length];
		for (int i = 0; i < firstMatrix.length; i++) {
			for (int k = 0; k < firstMatrix[0].length; k++) {
				double firstVal = firstMatrix[i][k];
				for (int j = 0; j < secondMatrix[0].length; j++) {
					result[i][j] += firstVal * secondMatrix[k][j];
				}
			}
		}

		return result;
	}

	private static double[][] randomMatrix(int n, int m) {
		double[][] M = new double[n][m];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < m; j++)
				M[i][j] = Math.random();
		return M;
	}

	public static void findOptimalThreadCount(double[][] firstMatrix, double[][] secondMatrix) {
		int maxThreads = Runtime.getRuntime().availableProcessors();
		long bestTime = Long.MAX_VALUE;
		int optimalThreads = 1;
		int exps = 10;

		System.out.println("Максимальное количество потоков = " + maxThreads);

		for (int threads = 1; threads <= maxThreads; threads++) {
			numThreads = threads;
			long totalTime = 0;

			for (int i = 0; i < exps; i++) {
				long startTime = System.currentTimeMillis();
				multiplyParallel(firstMatrix, secondMatrix);
				long endTime = System.currentTimeMillis();
				totalTime += (endTime - startTime);
			}

			long avgTime = totalTime / exps;
			System.out.println("Потоков: " + threads + ", Среднее время: " + avgTime + " мс");

			if (avgTime < bestTime) {
				bestTime = avgTime;
				optimalThreads = threads;
			}
		}

		numThreads = optimalThreads;

		System.out.println("\n-------------------------------------------------------------");
		System.out
				.println("Оптимальное число потоков: " + optimalThreads + ", со средним временем: " + bestTime + " мс");
	}

	public static void main(String[] args) {
		int size = 1500;

		double[][] A = randomMatrix(size, size);
		double[][] B = randomMatrix(size, size);

		System.out.println("----------- Поиск оптимального количества потоков -----------");
		findOptimalThreadCount(A, B);

		long t1 = System.currentTimeMillis();
		multiply(A, B);
		long singleTime = System.currentTimeMillis() - t1;

		long t2 = System.currentTimeMillis();
		multiplyParallel(A, B);
		long parallelTime = System.currentTimeMillis() - t2;

		System.out.println("Однопоточное умножение: " + singleTime + " мс");
		System.out.println("Многопоточное умножение: " + parallelTime + " мс");
	}
}
