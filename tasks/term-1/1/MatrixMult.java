public class MatrixMult {

	public static double[][] createMatrix(int rows, int cols, double minValue, double maxValue) {
		double[][] matrix = new double[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = Math.random() * (maxValue - minValue + 1) + minValue;
			}
		}
		return matrix;
	}

	public static void printMatrix(double[][] matrix) {
		try {
			if (matrix == null) {
				throw new IllegalArgumentException("Null matrix can't be printed!");
			}

			System.out.print("Matrix:\n");

			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					System.out.printf("%.4f\t", matrix[i][j]);
				}
				System.out.print("\n");
			}
		
		}
		catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		double[][] result = new double[0][0];
		
		try {
			if (firstMatrix == null || secondMatrix == null) {
				throw new IllegalArgumentException("The matrix being multiplied cannot be null!");
			}

			int rowsFirstMatrix = firstMatrix.length;
			int colsFirstMatrix = firstMatrix[0].length;
			int rowsSecondMatrix = secondMatrix.length;
			int colsSecondMatrix = secondMatrix[0].length;

			if (colsFirstMatrix != rowsSecondMatrix) {
				throw new IllegalArgumentException("Matrix can't be multiplied!");
			}

			double[][] resultMatrix = new double[rowsFirstMatrix][colsSecondMatrix];

			for (int i = 0; i < rowsFirstMatrix; i++) {
				for (int j = 0; j < colsSecondMatrix; j++) {
					for (int k = 0; k < colsFirstMatrix; k++) {
						resultMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
					}
				}
			}

			result = resultMatrix;

		} catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
		}
		
		
		return result;
	}
	
	public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
		double[][] result = new double[0][0];
		
		try {
			if (firstMatrix == null || secondMatrix == null) {
				throw new IllegalArgumentException("The matrix being multiplied cannot be null!");
			}

			int rowsFirstMatrix = firstMatrix.length;
			int colsFirstMatrix = firstMatrix[0].length;
			int rowsSecondMatrix = secondMatrix.length;
			int colsSecondMatrix = secondMatrix[0].length;

			if (colsFirstMatrix != rowsSecondMatrix) {
				throw new IllegalArgumentException("Matrix can't be multiplied!");
			}

			double[][] resultMatrix = new double[rowsFirstMatrix][colsSecondMatrix];

			for (int i = 0; i < rowsFirstMatrix; i++) {
				for (int k = 0; k < colsFirstMatrix; k++) {
					double value = firstMatrix[i][k];
					for (int j = 0; j < colsSecondMatrix; j++) {
						resultMatrix[i][j] += value * secondMatrix[k][j];
					}
				}
			}

			result = resultMatrix;

		} catch (IllegalArgumentException e) {
			System.err.println("Exception: " + e.getMessage());
		}
		
		
		return result;
	}

	public static void main(String[] args) {
			long totalTime = 0;
			int runsNum = 20;
			int size = 1000;

			double[][] matrix1 = createMatrix(size, size, 0, 100);
			double[][] matrix2 = createMatrix(size, size, 0, 100);
			 
			for (int i = 0; i < runsNum; i++) {
				long startTime = System.currentTimeMillis();
				multiplyOptimized(matrix1, matrix2);
				long endTime = System.currentTimeMillis();
				totalTime += (endTime - startTime);
			}

			System.out.println("Total time of runs: " + totalTime + " ms");
			System.out.println("Average multiply execution time: " + totalTime/runsNum + " ms");
		}

}
