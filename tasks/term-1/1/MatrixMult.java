public class MatrixMult {

    private static final int DEFAULT_BLOCK_SIZE = 128;

    private static boolean isInvalidMatrix(double[][] matrix) {
        if (matrix == null) {
            return true;
        }

        int cols = matrix[0].length;
        for (double[] matrixRow: matrix) {
            if (matrixRow.length != cols) {
                return true;
            }
        }

        return false;
    }

    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    public static void measureMultiplicationPerformance(int matrixSize, int iterations) {
        if (matrixSize <= 0 || iterations <= 0) {
            throw new IllegalArgumentException("Matrix size and iterations must be positive.");
        }

        System.out.println("Testing performance for " +
                matrixSize + "x" + matrixSize + " matrices over " + iterations + " iterations");

        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            double[][] matrix1 = generateRandomMatrix(matrixSize, matrixSize);
            double[][] matrix2 = generateRandomMatrix(matrixSize, matrixSize);

            long startTime = System.nanoTime();

            multiply(matrix1, matrix2);

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            totalTime += duration;
        }

        double avgTimeMs = totalTime / (iterations * 1_000_000.0);
        System.out.printf("Average time: %.2f ms%n", avgTimeMs);
    }

    /**
     * Straightforward multiplication algorithm for matrices multiplication.<br><br>
     * <note>
     * For the best cache usage tried to save rows and cols in memory to ensure indexation amount as low as possible
     * </note>
     * @param firstMatrix is the first matrix in the multiplication
     * @param secondMatrix is the second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    private static double[][] straightforwardMultiply(double[][] firstMatrix, double[][] secondMatrix) {
        int firstMatrixRows = firstMatrix.length;
        int firstMatrixCols = firstMatrix[0].length;

        int secondMatrixRows = secondMatrix.length;
        int secondMatrixCols = secondMatrix[0].length;

        double[][] secondMatrixTranspose = new double[secondMatrixCols][secondMatrixRows];
        for (int i = 0; i < secondMatrixRows; i++) {
            for (int j = 0; j < secondMatrixCols; j++) {
                secondMatrixTranspose[j][i] = secondMatrix[i][j];
            }
        }

        double[][] resultData = new double[firstMatrixRows][secondMatrixCols];

        for (int i = 0; i < firstMatrixRows; i++) {
            double[] firstMatrixRow = firstMatrix[i];
            double[] resultRow = resultData[i];

            for (int j = 0; j < secondMatrixCols; j++) {
                double[] secondMatrixCol = secondMatrixTranspose[j];

                double sum = 0.0;
                for (int k = 0; k < firstMatrixCols; k++) {
                    sum += firstMatrixRow[k] * secondMatrixCol[k];
                }
                resultRow[j] = sum;
            }
        }

        return resultData;
    }

    /**
     * Function uses block matrix multiplication algorithm for the best performance for very big matrices.<br><br>
     * @param firstMatrix is the first matrix in the multiplication
     * @param secondMatrix is the second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    private static double[][] multiplyMatricesByBlocks(double[][] firstMatrix, double[][] secondMatrix) {
        // Matrix block multiplication.
        // Block size of 128 is calculated through trial and error to ensure the best performance possible.
        // Some benchmarks using measureMultiplicationPerformance:
        //  1. 1000x1000 - <140ms (average from 50 iterations)
        //  2. 2000x2000 - ~1s (average from 50 iterations)
        //  3. 5000x5000 - ~60s (average fom 10 iterations)
        
        int firstMatrixRows = firstMatrix.length;
        int firstMatrixCols = firstMatrix[0].length;

        int secondMatrixCols = secondMatrix[0].length;

        double[][] resultData = new double[firstMatrixRows][secondMatrixCols];

        for (int i0 = 0; i0 < firstMatrixRows; i0 += DEFAULT_BLOCK_SIZE) {
            int i1 = Math.min(i0 + DEFAULT_BLOCK_SIZE, firstMatrixRows);

            for (int j0 = 0; j0 < secondMatrixCols; j0 += DEFAULT_BLOCK_SIZE) {
                int j1 = Math.min(j0 + DEFAULT_BLOCK_SIZE, secondMatrixCols);

                for (int k0 = 0; k0 < firstMatrixCols; k0 += DEFAULT_BLOCK_SIZE) {
                    int k1 = Math.min(k0 + DEFAULT_BLOCK_SIZE, firstMatrixCols);

                    for (int i = i0; i < i1; i++) {
                        double[] resultRow = resultData[i];
                        double[] firstMatrixRow = firstMatrix[i];

                        for (int k = k0; k < k1; k++) {
                            double element = firstMatrixRow[k];
                            double[] secondMatrixRow = secondMatrix[k];

                            for (int j = j0; j < j1; j++) {
                                resultRow[j] += element * secondMatrixRow[j];
                            }
                        }
                    }
                }
            }
        }

        return resultData;
    }

    /**
     * Heuristic for the multiplication algorithm choice is estimation for operations,
     * needed to multiply both matrices.<br><br>
     * If matrix <code>a</code> is MxN, then <code>b</code> must be NxK to correctly multiply both of them.<br><br>
     * This function simply mean: <code>this * other</code>, where <code>this</code> is a matrix calling the function
     * and <code>other</code> is a parameter passing to it.
     * @param firstMatrix is not null object representing first matrix in the multiplication
     * @param secondMatrix is not null object representing second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        try {
            if (isInvalidMatrix(firstMatrix)) {
                throw new IllegalArgumentException(
                        "First matrix is not valid. " +
                                "You should pass a valid matrix to multiply them!"
                );
            }

            if (isInvalidMatrix(secondMatrix)) {
                throw new IllegalArgumentException(
                        "Second matrix is not valid. " +
                                "You should pass a valid matrix to multiply them!"
                );
            }

            if (firstMatrix[0].length != secondMatrix.length) {
                throw new IllegalArgumentException(
                        "Column amount of the first matrix " +
                                "is not equal to the row amount of the second matrix!"
                );
            }
        }
        catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return null;
        }

        if (firstMatrix.length > 1000 && firstMatrix[0].length > 1000 && secondMatrix[0].length > 1000) {
            return straightforwardMultiply(firstMatrix, secondMatrix);
        }
        return multiplyMatricesByBlocks(firstMatrix, secondMatrix);
    }

    public static void main(String[] args) {
        measureMultiplicationPerformance(10, 100);
        System.out.println();
        measureMultiplicationPerformance(25, 100);
        System.out.println();
        measureMultiplicationPerformance(100, 100);
        System.out.println();
        measureMultiplicationPerformance(500, 10);
        System.out.println();
        measureMultiplicationPerformance(1000, 10);
        System.out.println();
//        measureMultiplicationPerformance(1500, 10);
    }

}
