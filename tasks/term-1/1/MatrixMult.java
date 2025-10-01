public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        try {
			if (colsA != rowsB) {
				throw new IllegalArgumentException(
					"Matrices can't be multiplied!" + 
					"The number of first matrix's columns (" + 
					colsA + ") should match the number of second matrix's rows (" + rowsB + ")"
				);
			}
		} catch (IllegalArgumentException err) {
			System.out.println("Error: " + err.getMessage());
			return null;
		}
        
        double[][] result = new double[rowsA][colsB];
        
        for (int k = 0; k < colsA; k++) {
            for (int i = 0; i < rowsA; i++) {
                double val = firstMatrix[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += val * secondMatrix[k][j];
                }
            }
        }
        
        return result;
	}

	public static double[][] createRandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
    }

	public static void printMatrix(double[][] matrix, String name) {
        if (matrix == null) {
            System.out.println(name + ": Matrix is empty");
            return;
        }
        
        System.out.println("\n" + name + " (" + matrix.length + "x" + matrix[0].length + "):");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%.3f ", matrix[i][j]);
            }
            System.out.println();
        }
    }

	public static void main(String[] args) {
        int size = 2000;
        int numExperiments = 10;

        double[][] matrixA = createRandomMatrix(size, size, 1, 100);
        double[][] matrixB = createRandomMatrix(size, size, 1, 100);

        long totalTime = 0;

        for (int exp = 0; exp < numExperiments; exp++){
            long startTime = System.currentTimeMillis();
            multiply(matrixA, matrixB);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            System.out.println("Execution time for iteration " + (exp + 1) + ": " + duration + " ms");
        }

        System.out.println("=== RESULTS ===");
        System.out.println("Total execution time for all multiplications: " + totalTime + " ms");
        System.out.println("Average execution time: " + (totalTime / numExperiments) + " ms");
    }
}
