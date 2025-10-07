public class MatrixMult {

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
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				for (int k = 0; k < firstMatrix[0].length; k++) {
					result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
				}
			}
		}
		return result;
	}

	public static double[][] multiplyPro(double[][] firstMatrix, double[][] secondMatrix) {
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

	public static void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println("");
		}
	}

	public static double[][] createMatrix(int size) {
		double[][] result = new double[size][size];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				result[i][j] = Math.round(Math.random() * 10);
			}
		}
		return result;
	}

	public static void main(String[] args) {

		int[] sizes = { 100, 500, 1000, 2000 };

		for (int i : sizes) {
			double[][] m1 = createMatrix(i), m2 = createMatrix(i);
			long startTime = System.currentTimeMillis();
			double[][] m3 = multiplyPro(m1, m2);
			long endTime = System.currentTimeMillis();

			System.out.printf("Время умножения матриц размером %dx%d составляет %d ms\n", i, i, endTime - startTime);
		}

	}
}
