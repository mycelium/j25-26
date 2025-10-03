import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (secondMatrix.length != n) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }
        
        double[][] result = new double[m][p];
        
        // Оптимизация: 
        // инициализация перменной раз для последнего цикла -> отсутствие ненужного повторного обращения
        // меняем порядок циклов для лучшей локальности данных
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
    
	public static double[][] multiplyStandard(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (secondMatrix.length != n) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }
        
        double[][] result = new double[m][p];
        
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < p; k++) 
                {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j]; 
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

	public record TimeMatrixResult(Long time, double[][] matrix) {}

	public static void main(String[] args) 
	{   
		int size = 1000;

		double[][] A = createRandomMatrix(size, size);
		double[][] B = createRandomMatrix(size, size);

		var executorService = Executors.newFixedThreadPool(2);

		var normalResult = executorService.submit(() -> {
			var start = System.currentTimeMillis();
			var matrix = multiply(A, B);
			var time = System.currentTimeMillis() - start;
			return new TimeMatrixResult(time, matrix);
		});

		var strassenResult = executorService.submit(() -> {
			var start = System.currentTimeMillis();
			var matrix = multiplyStandard(A, B);
			var time = System.currentTimeMillis() - start;
			return new TimeMatrixResult(time, matrix);
		});

		executorService.shutdown();

		try {

			var result1 = normalResult.get();
			var result2 = strassenResult.get();

			if (Arrays.deepEquals(result1.matrix, result2.matrix)) 
			{
				System.out.printf("Время выполнения оптимизированного умножения: %d мс%n", result1.time);
				System.out.printf("Время выполнения обычного умножения: %d мс%n", result2.time);
			}
			else
			{
				System.err.println("матрицы разные, проверьте алгоритмы");
			}

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

}
