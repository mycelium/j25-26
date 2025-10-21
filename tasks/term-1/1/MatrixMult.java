public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
		int firstRowCount = firstMatrix.length;
		int firstColCount = firstMatrix[0].length;
	 	int secondRowCount = secondMatrix.length;
		int secondColCount = secondMatrix[0].length;

		double[][] result = new double[firstRowCount][secondColCount];

		if (firstColCount != secondRowCount) {
			throw new IllegalArgumentException("Incorrect sizes for multiply");
		}

		/*
		//наивный метод
		for (int i = 0; i < firstRowCount; i++) {
			for (int j = 0; j < secondColCount; j++) {
				double sum = 0;
				for (int k = 0; k < firstColCount; k++) {
					sum += firstMatrix[i][k] * secondMatrix[k][j];
				}
				result[i][j] = sum;
			}
		}
		*/
		//оптимизация
		for (int i = 0; i < firstRowCount; i++) {
			for (int k = 0; k < firstColCount; k++) {
				double tmpVal = firstMatrix[i][k];
				for (int j = 0; j < secondColCount; j++) {
					result[i][j] += tmpVal * secondMatrix[k][j];
				}
			}
		}
		

		//printMatrix(result);
		return result;
	}

	public static double[][] generateMatrix(int rows, int cols) {
		int min = 10;
		int max = 100;
		double[][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = (Math.random() * (max - min + 1)) + min;
			}
		}
		return result;
	}

	public static void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf("%-10.2f", matrix[i][j]);
			}
			System.out.println();
        	}
	}

	public static void main(String[] args) {
		int size = 500;
		int repeat = 10;
		
		long totalTime = 0;
		
		for (int i = 0; i < repeat; i++) {
			double[][] A = generateMatrix(size, size);
			//printMatrix(A);
			double[][] B = generateMatrix(size, size);
			//printMatrix(B);
			
			long startTime = System.currentTimeMillis();
			double[][] C = multiply(A, B);
			long endTime = System.currentTimeMillis();
			totalTime += (endTime - startTime);
		}
        	

		double averageTime = (double) totalTime / repeat;
		System.out.println("Average time (" + size + " x " + size + "): " + averageTime + " ms\n");
	}
}
