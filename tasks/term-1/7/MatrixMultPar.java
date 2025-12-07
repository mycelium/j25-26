
public class MatrixMultPar {
	
	// Стандартное однопоточное умножение матриц
	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int M = firstMatrix.length;
		int K = firstMatrix[0].length;
		int N = secondMatrix[0].length;
		
		// Проверка совместимости размеров
		if (K != secondMatrix.length) {
			throw new IllegalArgumentException(
				"Матрицы несовместимы для умножения. " +
				"Число столбцов первой матрицы (" + K + 
				") должно быть равно числу строк второй матрицы (" + secondMatrix.length + ")."
			);
		}

		double[][] resultMatrix = new double[M][N];
		

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				double sum = 0;
				for (int k = 0; k < K; k++) {
					sum += firstMatrix[i][k] * secondMatrix[k][j];
				}
				resultMatrix[i][j] = sum;
			}
		}
		
		return resultMatrix;
	}
	

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
		return multiplyParallel(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());
	}
	
	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int numThreads) {
	    int M = firstMatrix.length;
	    int K = firstMatrix[0].length;
	    int N = secondMatrix[0].length;

	    if (K != secondMatrix.length) {
	        throw new IllegalArgumentException("Матрицы не могут быть перемножены из-за несоответствия размеров.");
	    }

	    // Благодаря транспонированию процессор будет читать не по столбцам (прыгая по памяти), а по строкам (последовательно).
	    double[][] secondMatrixTransposed = new double[N][K];
	    for (int i = 0; i < K; i++) {
	        for (int j = 0; j < N; j++) {
	            secondMatrixTransposed[j][i] = secondMatrix[i][j];
	        }
	    }

	    double[][] resultMatrix = new double[M][N];
	    Thread[] threads = new Thread[numThreads];

	    int rowsPerThread = M / numThreads;
	    int remainingRows = M % numThreads;
	    int startRow = 0;

	    for (int t = 0; t < numThreads; t++) {
	        int rowsForThisThread = rowsPerThread + (t < remainingRows ? 1 : 0);
	        final int threadStartRow = startRow;
	        final int threadEndRow = startRow + rowsForThisThread;
	        startRow = threadEndRow;

	        threads[t] = new Thread(() -> {
	            for (int i = threadStartRow; i < threadEndRow; i++) {
	                double[] rowA = firstMatrix[i];
	                double[] rowResult = resultMatrix[i];

	                for (int j = 0; j < N; j++) {
	                    double[] rowB = secondMatrixTransposed[j];
	                    
	                    double sum = 0;
	                    for (int k = 0; k < K; k++) {
	                        sum += rowA[k] * rowB[k];
	                    }
	                    rowResult[j] = sum;
	                }
	            }
	        });
	        threads[t].start();
	    }

	    for (int t = 0; t < numThreads; t++) {
	        try {
	            threads[t].join();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }

	    return resultMatrix;
	}
	
	// Генерация случайной матрицы с заданными размерами
	public static double[][] generateRandomMatrix(int rows, int cols) {
		double[][] matrix = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random() * 200.0 - 100; 
			}
		}
		return matrix;
	}
	
	// Измерение среднего времени выполнения
	public static long measureAverageTime(double[][] A, double[][] B, int tries, boolean parallel, int numThreads) {
		long totalDuration = 0;
		for (int i = 0; i < tries; i++) {
			long startTime = System.nanoTime();
			if (parallel) {
				multiplyParallel(A, B, numThreads);
			} else {
				multiply(A, B);
			}
			long endTime = System.nanoTime();
			totalDuration += (endTime - startTime);
		}
		return totalDuration / tries;
	}
	
	public static void main(String[] args) {
		int[] sizes = {50, 100, 200, 500, 1000, 2000};
		int tries = 10;
		
		int availableCores = Runtime.getRuntime().availableProcessors();
		System.out.println("Количество доступных логических процессов: " + availableCores);
		

		// Прогрев
		double[][] warmA = generateRandomMatrix(500, 500); // Средний размер для прогрева
		double[][] warmB = generateRandomMatrix(500, 500);
		
		// Прогоняем 5 раз в многопоточном режиме, результат игнорируем
		for (int i = 0; i < 5; i++) {
			measureAverageTime(warmA, warmB, 1, true, availableCores);
		}
		
		System.out.println("Сравнение по " + tries + " запускам");
		
		for (int size : sizes) {
			System.out.println("\nРазмер матриц: " + size + "x" + size);
			double[][] A = generateRandomMatrix(size, size);
			double[][] B = generateRandomMatrix(size, size);
			
			long singleThreadTime = measureAverageTime(A, B, tries, false, 1);
			double singleThreadMs = singleThreadTime / 1_000_000.0;
			System.out.printf("Однопоточное: %8.2f мс%n", singleThreadMs);
		}
		
		for (int i = 4; i <= availableCores; i = i*2) {
			for (int size : sizes) {
				System.out.println("\nРазмер матриц: " + size + "x" + size);
				
				double[][] A = generateRandomMatrix(size, size);
				double[][] B = generateRandomMatrix(size, size);
				
				long parallelTime = measureAverageTime(A, B, tries, true, i);
				double parallelMs = parallelTime / 1_000_000.0;
				

				System.out.printf("Параллельное: %8.2f мс (%d потоков) %n", parallelMs, i);
			}
		}
	}
}
