import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class MatrixMultPar {

	static int numThreads = 0;
	
	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
		int m = firstMatrix.length;
		int n = firstMatrix[0].length;
		int p = secondMatrix[0].length;
		
		if (secondMatrix.length != n) {
			throw new IllegalArgumentException("Несовместимые размеры матриц");
		}
		
		double[][] result = new double[m][p];
		
		List<Thread> threads = new ArrayList<>();
		
		for (int threadId = 0; threadId < numThreads; threadId++) {
			final int currentThreadId = threadId;
			
			Thread thread = new Thread(() -> {
				for (int i = currentThreadId; i < m; i += numThreads) {
					for (int k = 0; k < n; k++) {
						double aik = firstMatrix[i][k];
						for (int j = 0; j < p; j++) {
							result[i][j] += aik * secondMatrix[k][j];
						}
					}
				}
			});
			
			threads.add(thread);
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Поток был прерван", e);
			}
		}
		
		return result;
	}

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        
		int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (secondMatrix.length != n) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }
        
        double[][] result = new double[m][p];
        
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                double aik = firstMatrix[i][k];
                for (int j = 0; j < p; j++) 
                {
                    result[i][j] += aik * secondMatrix[k][j];
                }
            }
        }
        
        return result;
    }

	public static double[][] createRandomMatrix(int rows, int cols) 
	{
		double[][] matrix = new double[rows][cols];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random();
			}
		}
		
		return matrix;
	}


	public static void main(String[] args) throws IOException, InterruptedException
	{   
		numThreads = 29;
		System.out.println("Количество потоков в pool: " + numThreads);

		warmUp(numThreads);
		
		int size = 1000;

		double[][] A = createRandomMatrix(size, size);
		double[][] B = createRandomMatrix(size, size);
					
		long start = System.currentTimeMillis();
		var matrix = multiply(A, B);
		long time = System.currentTimeMillis() - start;
					
		start = System.currentTimeMillis();
		var matrixParallel =  multiplyParallel(A, B);
		long timeParallel = System.currentTimeMillis() - start;

		if (Arrays.deepEquals(matrix, matrixParallel)) 
		{
			Double difference = (double) time / timeParallel;

			System.out.printf("Время выполнения обычного умножения: %d мс%n", time);
			System.out.printf("Время выполнения параллельного умножения: %d мс%n", timeParallel);
			System.out.printf("Ускорение: %f\n", difference);
		}

		try {
			measure();
		} catch (Exception e) {
			System.err.println("Ошибка анализа результатов сравнения");
		}
	}

	public static void measure() throws InterruptedException {
		
		double maxSum = -1;
		int maxThread = -1;

		for (int threadCount = 2; threadCount < 41; threadCount++) {
			numThreads = threadCount;
		
			double sum = 0;

			// Прогрев JVM
			warmUp(threadCount);
				
			for (int j = 0; j < 100; j++) {
				int size = 1000;

				double[][] A = createRandomMatrix(size, size);
				double[][] B = createRandomMatrix(size, size);

				// время на GC
				System.gc();
				Thread.sleep(50);

				long start = System.currentTimeMillis();
				multiply(A, B);
				long time = System.currentTimeMillis() - start;

				System.gc();
				Thread.sleep(50);
					
				start = System.currentTimeMillis();
				multiplyParallel(A, B);
				long timeParallel = System.currentTimeMillis() - start;

				double result = (double) time / timeParallel;
				sum += result;
				printAnimatedProgress(j, 100, String.format("Вычисление среднего ускорения для %d тредов", numThreads));
			}

			System.out.printf("\r%d - %f                                        \n", numThreads, sum / 100);
			
			if (maxSum <= sum) {
				maxSum = sum;
				maxThread = numThreads;
			}
		}

		System.out.printf("Максимальное среднее ускорение %f было получено для %d тредов\n", maxSum, maxThread);
	}
	
	// Несколько прогревочных итераций
	private static void warmUp(int threadCount) {
		int size = 500;
		double[][] A = createRandomMatrix(size, size);
		double[][] B = createRandomMatrix(size, size);
		
		ForkJoinPool warmupPool = new ForkJoinPool(threadCount);
		try {
			for (int i = 0; i < 3; i++) {
				multiply(A, B);
				warmupPool.submit(() -> multiplyParallel(A, B)).get();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			warmupPool.shutdown();
		}
	}

	private static final String[] ANIMATION = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    
    public static void printAnimatedProgress(int current, int total, String message) {
        float percent = (float) current / total;
        int animIndex = (int) ((System.currentTimeMillis() / 100) % ANIMATION.length);
        
        String bar = String.format("[%s] %.1f%% %s", ANIMATION[animIndex], percent * 100, message);
        
        System.out.print("\r" + bar);
    }
}
