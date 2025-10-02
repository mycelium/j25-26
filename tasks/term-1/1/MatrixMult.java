public class MatrixMult {

	public static double[][] multiply(double[][] A, double[][] B){
		try {
			int colsA = getCol(A);
			int colsB = getCol(B);
			int rowsA = getRow(A);
			int rowsB = getCol(B);
    		if (colsA != rowsB) {
        		throw new IllegalArgumentException("Matrices cannot be multiplied: " + "number of columns in the first matrix (" + colsA + ") " +
                                           "does not equal the number of rows in the second matrix (" + rowsB + ")");
    		}

			double[][] result = new double[rowsA][colsB];
			double[][] B_T = new double[rowsA][colsB];
    		for (int i = 0; i < colsA; i++) {
        		for (int j = 0; j < colsB; j++) {
            		B_T[j][i] = B[i][j];
        		}
    		}
			for (int i = 0; i < rowsA; i++) {
				for (int j = 0; j < colsB; j++) {
					double sum = 0;
					for (int k = 0; k < colsA; k++) {
						sum += A[i][k] * B_T[j][k];
					}
					result[i][j] = sum;
				}
			}
			return result;

		} catch (Exception e) {
    		System.err.println("Matrix multiplication error: " + e.getMessage());
    		return null; 
		}

	}

	// наивный метод
/*	public static double[][] multiplyMatrix(double[][] firstMatrix, double[][] secondMatrix){
		double[][] result = new double[getRow(firstMatrix)][getCol(secondMatrix)];
        for (int i = 0; i < getRow(firstMatrix); i++) {
            for (int j = 0; j < getCol(secondMatrix); j++) {
                for (int k = 0; k < getCol(firstMatrix); k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
		return result;
	}
*/

	private static int getRow(double[][] matrix){
		int row = matrix.length;
		return row;
	}

	private static int getCol(double[][] matrix){
		int col = matrix[0].length;
		return col;
	}

	public static double[][] createMatrix(int row, int col, int min, int max){
		double[][] matrix = new double[row][col];
		for (int i = 0; i < row; i++){
			for (int j = 0; j < col; j++){
				matrix[i][j] = Math.random() * (max - min + 1) + min;
			}
		}
		return matrix;
	}

	public static void printMatrix(double[][] matrix){
		if (matrix == null){
			System.out.println("Matrix is empty");
		}
		for (int i = 0; i < matrix.length; i++){
			for (int j = 0; j < matrix[i].length; j++){
				System.out.printf("%6.2f ", matrix[i][j]);
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {

	int[] sizes = { 100, 500, 1000, 2000 };
	int repetitions = 10;

	for (int size : sizes) {
		System.out.println("Testing size: " + size + "x" + size);
		long totalTime = 0;

		for (int i = 0; i < repetitions; i++) {
			double[][] m1 = createMatrix(size, size, 0, 10);
			double[][] m2 = createMatrix(size, size, 0, 10);

			long startTime = System.nanoTime();
			double[][] mres = multiply(m1, m2);
			long endTime = System.nanoTime();

			long duration = endTime - startTime;
			totalTime += duration;
		}
		double averageTime = totalTime / (double) repetitions;
		System.out.println("Average time for " + size + "x" + size + ": " + averageTime / 1_000_000 + " ms\n");
	} 	

}
}
