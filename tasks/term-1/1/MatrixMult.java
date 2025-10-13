
import java.util.stream.IntStream;

public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int firstRows = firstMatrix.length;
		int firstCols = firstMatrix[0].length;
		int secondRows = secondMatrix.length;
		int secondCols = secondMatrix[0].length;

		if (firstCols != secondRows){
			throw new IllegalArgumentException("Данные матрицы нельзя умножить! Число столбцов первой матрицы должно быть равно числу столбцов второй матрицы.");
		}
		
		double[][] resMatrix = new double[firstRows][secondCols];
		for (int y = 0; y < firstRows; y++){
			for (int x = 0; x < secondCols; x++){
				double s = 0;
				for (int c = 0; c < firstCols; c++){
					s += firstMatrix[y][c] * secondMatrix[c][x];
				}
				resMatrix[y][x] = s;
			}
		}
		
		return resMatrix;
	}
	
	public static void printMatrix(double[][] matrix){
		for (double[] row : matrix){
			String rowStr = "";
			for (double value : row){
				rowStr += String.valueOf(value) + " ";
			}
			System.out.println(rowStr);
		}
	}

	public static double[][] generateRandomMatrix(int rows, int cols){
		double[][] resMatrix = new double[rows][cols];
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				resMatrix[y][x] = Math.random();
			}
		}
		return resMatrix;
	}

	private static double[][] transpose(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double[][] resMatrix = new double[cols][rows];
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				resMatrix[x][y] = matrix[y][x];
			}
		}
		return resMatrix;
	}

	public static double[][] multiplyTransposeParallel(double[][] firstMatrix, double[][] secondMatrix) {
		int firstRows = firstMatrix.length;
		int firstCols = firstMatrix[0].length;
		int secondRows = secondMatrix.length;
		int secondCols = secondMatrix[0].length;

		if (firstCols != secondRows) {
			throw new IllegalArgumentException("Данные матрицы нельзя умножить! Число столбцов первой матрицы должно быть равно числу столбцов второй матрицы.");
		}

		double[][] secondMatrixTrans = transpose(secondMatrix);
		double[][] resMatrix = new double[firstRows][secondCols];
		
		IntStream.range(0, firstRows)
		.parallel()
		.forEach(y -> {
			for (int x = 0; x < secondCols; x++) {
				double s = 0.0;
				for (int c = 0; c < firstCols; c++) {
					s += firstMatrix[y][c] * secondMatrixTrans[x][c];
				}
				resMatrix[y][x] = s;
			}
		});
		return resMatrix;
	}

	public static void main(String args[]){
		long sum_standart = 0;
		long sum_opt = 0;
		int count = 10;
		for (int i = 0; i < count; i++){
			double[][] A = generateRandomMatrix(1000, 1600);
			double[][] B = generateRandomMatrix(1600, 1800);

			long start = System.currentTimeMillis();
			multiply(A, B);
			sum_standart += System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			multiplyTransposeParallel(A, B);
			sum_opt += System.currentTimeMillis() - start;
		}
		System.out.println("Среднее время для стандартного умножения (10 тестов): " + (sum_standart / count));
		System.out.println("Среднее время для оптимизированного умножения (10 тестов): " + (sum_opt / count));
	}
}



