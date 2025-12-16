
public class MatrixMultPar {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
		return multiplyParallel(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());
	}

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}

		int aRows = firstMatrix.length;
		int aCols = firstMatrix[0].length;
		int bRows = secondMatrix.length;
		int bCols = secondMatrix[0].length;

		if (aCols != bRows) {
			throw new IllegalArgumentException("Несовместимые размеры матриц: " +
				aCols + " != " + bRows);
		}

		double[][] result = new double[aRows][bCols];

		// Разделяем работу по строкам между потоками
		int rowsPerThread = aRows / numThreads;
		int extraRows = aRows % numThreads;

		// Создаем массив потоков вручную
		Thread[] threads = new Thread[numThreads];
		int startRow = 0;

		for (int thread = 0; thread < numThreads; thread++) {
			int endRow = startRow + rowsPerThread + (thread < extraRows ? 1 : 0);

			final int threadStartRow = startRow;
			final int threadEndRow = endRow;

			// Создаем задачу для потока
			Runnable task = () -> {
				// Каждый поток вычисляет свою часть строк
				for (int i = threadStartRow; i < threadEndRow; i++) {
					for (int k = 0; k < aCols; k++) {
						double aik = firstMatrix[i][k];
						for (int j = 0; j < bCols; j++) {
							result[i][j] += aik * secondMatrix[k][j];
						}
					}
				}
			};

			// Создаем и сохраняем поток
			threads[thread] = new Thread(task);
			startRow = endRow;
		}

		// Запускаем все потоки
		for (Thread thread : threads) {
			thread.start();
		}

		// Ожидаем завершения всех потоков
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException("Ошибка при выполнении параллельного умножения", e);
			}
		}
		return result;
	}

	// Однопоточная версия для сравнения (скопирована из лабы 1)
	public static double[][] multiplySequential(double[][] firstMatrix, double[][] secondMatrix) {
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}

		int aRows = firstMatrix.length;
		int aCols = firstMatrix[0].length;
		int bRows = secondMatrix.length;
		int bCols = secondMatrix[0].length;

		if (aCols != bRows) {
			throw new IllegalArgumentException("Несовместимые размеры матриц: " +
				aCols + " != " + bRows);
		}

		double[][] result = new double[aRows][bCols];

		for (int i = 0; i < aRows; i++) {
			for (int k = 0; k < aCols; k++) {
				double aik = firstMatrix[i][k];
				for (int j = 0; j < bCols; j++) {
					result[i][j] += aik * secondMatrix[k][j];
				}
			}
		}

		return result;
	}

	// Метод для подбора оптимального количества потоков
	public static int findOptimalThreadCount(double[][] matrixA, double[][] matrixB) {
		int maxThreads = Runtime.getRuntime().availableProcessors() * 2; // Тестируем до 2x ядер
		long bestTime = Long.MAX_VALUE;
		int optimalThreads = 1;

		System.out.println("Подбор оптимального количества потоков...");
		System.out.println("Потоки | Время (мс) | Ускорение");
		System.out.println("--------|-----------|----------");

		// Сначала замеряем однопоточную версию
		long sequentialTime = measureTime(() -> multiplySequential(matrixA, matrixB), 10);
		System.out.printf("%7d | %9d | %.2fx%n", 1, sequentialTime, 1.0);

		// Тестируем разное количество потоков
		for (int threads = 2; threads <= maxThreads; threads++) {
			final int numThreads = threads; // Создаем effectively final копию
			long time = measureTime(() -> multiplyParallel(matrixA, matrixB, numThreads), 10);
			double speedup = (double) sequentialTime / time;

			System.out.printf("%7d | %9d | %.2fx%n", threads, time, speedup);

			if (time < bestTime) {
				bestTime = time;
				optimalThreads = threads;
			}
		}

		System.out.println("Оптимальное количество потоков: " + optimalThreads);
		return optimalThreads;
	}

	// Метод для измерения времени выполнения
	private static long measureTime(Runnable task, int iterations) {
		// Прогрев JVM
		for (int i = 0; i < 2; i++) {
			task.run();
		}

		long totalTime = 0;
		for (int i = 0; i < iterations; i++) {
			long startTime = System.nanoTime();
			task.run();
			long endTime = System.nanoTime();
			totalTime += (endTime - startTime) / 1_000_000; // в миллисекундах
		}

		return totalTime / iterations;
	}

	// Генерация случайной матрицы
	private static double[][] generateRandomMatrix(int rows, int cols) {
		double[][] matrix = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random() * 100;
			}
		}
		return matrix;
	}

	// Проверка равенства матриц с допустимой погрешностью
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
		System.out.println("=== Параллельное умножение матриц ===\n");

		// Тестируем на матрицах разных размеров
		int[] sizes = {500, 1000, 1500};

		for (int size : sizes) {
			System.out.println("Тестирование матриц " + size + "x" + size);
			System.out.println("=".repeat(50));

			// Генерируем тестовые матрицы
			double[][] matrixA = generateRandomMatrix(size, size);
			double[][] matrixB = generateRandomMatrix(size, size);

			// Подбираем оптимальное количество потоков
			int optimalThreads = findOptimalThreadCount(matrixA, matrixB);

			System.out.println("\nСравнение производительности:");
			System.out.println("| Метод              | Время (мс) | Ускорение |");
			System.out.println("|--------------------|------------|-----------|");

			// Замер однопоточной версии
			long sequentialTime = measureTime(() -> multiplySequential(matrixA, matrixB), 10);
			System.out.printf("| Однопоточная       | %10d | %.2fx     |%n", sequentialTime, 1.0);

			// Замер параллельной версии с оптимальным количеством потоков
			long parallelTime = measureTime(() -> multiplyParallel(matrixA, matrixB, optimalThreads), 10);
			double speedup = (double) sequentialTime / parallelTime;
			System.out.printf("| Параллельная (%2d)  | %10d | %.2fx     |%n",
				optimalThreads, parallelTime, speedup);

			// Проверка корректности результатов
			double[][] seqResult = multiplySequential(matrixA, matrixB);
			double[][] parResult = multiplyParallel(matrixA, matrixB, optimalThreads);

			if (matricesEqual(seqResult, parResult, 1e-10)) {
				System.out.println("✓ Результаты совпадают");
			} else {
				System.out.println("✗ Результаты отличаются!");
			}

			System.out.println();
		}

		System.out.println("Информация о системе:");
		System.out.println("- Доступно ядер процессора: " + Runtime.getRuntime().availableProcessors());
		System.out.println("- Максимальная память JVM: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
	}
}
