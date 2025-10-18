import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class MatrixMultPar {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
		int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (secondMatrix.length != n) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }
        
        double[][] result = new double[m][p];

		IntStream.range(0, m).parallel().forEach(
			i -> {
				for (int k = 0; k < n; k++) {
					double aik = firstMatrix[i][k];

					for (int j = 0; j < p; j++) 
					{
						result[i][j] += aik * secondMatrix[k][j];
					}
				}
			}
		);
        
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
		int size = 1000;

		double[][] A = createRandomMatrix(size, size);
		double[][] B = createRandomMatrix(size, size);
					
		long start = System.currentTimeMillis();
				
		var matrix = multiply(A, B);
		long time = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		var matrixParallel = multiplyParallel(A, B);
		long timeParallel = System.currentTimeMillis() - start;

		if (Arrays.deepEquals(matrix, matrixParallel)) 
		{
			Double result = (double) time / timeParallel;

			System.out.printf("Время выполнения обычного умножения: %d мс%n", time);
			System.out.printf("Время выполнения параллельного умножения: %d мс%n", timeParallel);
			System.out.printf("Ускорение: %f\n", result);
		}
		// try {
		// 	measure();
		// 	printMid();
		// } catch (Exception e) {
		// 	System.err.println("Ошибка анализа результатов сравнения");
		// }
	}

	public static void printMid() throws IOException{
		for (Integer i = 2; i < 41; i++) {
			DoubleSummaryStatistics stats = Files.readAllLines(Paths.get("results_" + i.toString()))
                                        .stream()
                                        .mapToDouble(Double::parseDouble)
                                        .summaryStatistics();
    
    		System.out.printf("%d - %f\n", i, stats.getAverage());
		}
	}

	public static void measure() throws IOException, InterruptedException {
		for (int threadCount = 2; threadCount < 41; threadCount++) {
			
			File file = new File("./results_" + threadCount);
			file.delete();
			file.createNewFile();

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
				
				// Прогрев JVM
				System.out.println("Прогрев для " + threadCount + " потоков...");
				warmUp(threadCount);
				
				for (int j = 0; j < 30; j++) {
					int size = 1000;

					double[][] A = createRandomMatrix(size, size);
					double[][] B = createRandomMatrix(size, size);

					// время на GC
					System.gc();
					Thread.sleep(50);

					long start = System.currentTimeMillis();
					var matrix = multiply(A, B);
					long time = System.currentTimeMillis() - start;

					System.gc();
					Thread.sleep(50);
					
					long timeParallel = measureParallelMultiplication(A, B, threadCount);

					double result = (double) time / timeParallel;
					writer.write(String.valueOf(result) + '\n');
					System.out.printf("Тест %d: Ускорение = %.2f%n", j + 1, result);
				}
			}
		}
	}

	private static long measureParallelMultiplication(double[][] A, double[][] B, int threadCount) {
		ForkJoinPool customPool = new ForkJoinPool(threadCount);
		try {
			long start = System.currentTimeMillis();
			customPool.submit(() -> {
				multiplyParallel(A, B);
			}).get();
			return System.currentTimeMillis() - start;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			customPool.shutdown();
		}
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

}
