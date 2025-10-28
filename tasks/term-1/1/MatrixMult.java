class Matrix {

    private static final int DEFAULT_BLOCK_SIZE = 128;

    private final double[][] data;

    private final int rows;
    private final int cols;

    private boolean isInvalidMatrix(double[][] matrix_data) {
        return matrix_data == null || matrix_data.length == 0 || matrix_data[0].length == 0;
    }

    /**
     * Straightforward multiplication algorithm for matrices multiplication.<br><br>
     * <note>
     * For the best cache usage tried to save rows and cols in memory to ensure indexation amount as low as possible
     * </note>
     * @param other is second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    private double[][] straightforwardMultiply(Matrix other) {
        double[][] otherTranspose = new double[other.cols][other.rows];
        for (int i = 0; i < other.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                otherTranspose[j][i] = other.data[i][j];
            }
        }

        double[][] resultData = new double[this.rows][other.cols];

        for (int i = 0; i < this.rows; i++) {
            double[] thisRow = this.data[i];
            double[] resultRow = resultData[i];

            for (int j = 0; j < other.cols; j++) {
                double[] otherCol = otherTranspose[j];

                double sum = 0.0;
                for (int k = 0; k < this.cols; k++) {
                    sum += thisRow[k] * otherCol[k];
                }
                resultRow[j] = sum;
            }
        }

        return resultData;
    }

    /**
     * Function uses block matrix multiplication algorithm for the best performance for very big matrices.<br><br>
     * @param other is second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    private double[][] multiplyMatricesByBlocks(Matrix other) {
        // Matrix block multiplication.
        // Block size of 128 is calculated through trial and error to ensure the best performance possible.
        // Some benchmarks using measureMultiplicationPerformance:
        //  1. 1000x1000 - <140ms (average from 50 iterations)
        //  2. 2000x2000 - ~1s (average from 50 iterations)
        //  3. 5000x5000 - ~60s (average fom 10 iterations)

        double[][] resultData = new double[this.rows][other.cols];

        for (int i0 = 0; i0 < this.rows; i0 += DEFAULT_BLOCK_SIZE) {
            int i1 = Math.min(i0 + DEFAULT_BLOCK_SIZE, this.rows);

            for (int j0 = 0; j0 < other.cols; j0 += DEFAULT_BLOCK_SIZE) {
                int j1 = Math.min(j0 + DEFAULT_BLOCK_SIZE, other.cols);

                for (int k0 = 0; k0 < this.cols; k0 += DEFAULT_BLOCK_SIZE) {
                    int k1 = Math.min(k0 + DEFAULT_BLOCK_SIZE, this.cols);

                    for (int i = i0; i < i1; i++) {
                        double[] resultRow = resultData[i];
                        double[] thisRow = this.data[i];

                        for (int k = k0; k < k1; k++) {
                            double element = thisRow[k];
                            double[] otherRow = other.data[k];

                            for (int j = j0; j < j1; j++) {
                                resultRow[j] += element * otherRow[j];
                            }
                        }
                    }
                }
            }
        }

        return resultData;
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

    public Matrix(double[][] data) {
        if (isInvalidMatrix(data)) {
            throw new IllegalArgumentException("Matrix can not be empty.");
        }

        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            if (data[i].length != this.cols) {
                throw new IllegalArgumentException("Rows does not have the same length.");
            }

            System.arraycopy(data[i], 0, this.data[i], 0, this.cols);
        }
    }

    /**
     * Heuristic for the multiplication algorithm choice is estimation for operations,
     * needed to multiply both matrices.<br><br>
     * If matrix <code>a</code> is MxN, then <code>b</code> must be NxK to correctly multiply both of them.<br><br>
     * This function simply mean: <code>this * other</code>, where <code>this</code> is a matrix calling the function
     * and <code>other</code> is a parameter passing to it.
     * @param other is not null object representing second matrix in the multiplication
     * @return new matrix (result of the multiplication of corresponding matrices).
     */
    public double[][] multiply(Matrix other) {
        if (other == null) {
            throw new IllegalArgumentException("Matrix can not be null.");
        }
        if (isInvalidMatrix(other.data)) {
            throw new IllegalArgumentException("Matrix can not be empty.");
        }

        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                    "Multiplication is not possible due to size differences " +
                            "(columns " + this.cols + " not equal to rows " + other.rows + ")."
            );
        }

        long operation_amount_estimate = (long) this.rows * this.cols * other.cols;
        final long BLOCKED_OPERATION_THRESHOLD = 50_000L;

        if (operation_amount_estimate <= BLOCKED_OPERATION_THRESHOLD) {
            return straightforwardMultiply(other);
        }
        return multiplyMatricesByBlocks(other);
    }

    public void displayMatrix() {
        System.out.println("Matrix " + getRows() + "x" + getCols());
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                System.out.printf("%8.2f", data[i][j]);
            }
            System.out.println();
        }
    }

    public static double measureMultiplicationPerformance(int matrixSize, int iterations) {
        if (matrixSize <= 0 || iterations <= 0) {
            throw new IllegalArgumentException("Matrix size and iterations must be positive.");
        }

        System.out.println("Testing performance for " +
                matrixSize + "x" + matrixSize + " matrices over " + iterations + " iterations");

        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            Matrix matrix1 = new Matrix(generateRandomMatrix(matrixSize, matrixSize));
            Matrix matrix2 = new Matrix(generateRandomMatrix(matrixSize, matrixSize));

            long startTime = System.nanoTime();

            matrix1.multiply(matrix2);

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            totalTime += duration;
        }

        double avgTimeMs = totalTime / (iterations * 1_000_000.0);
        System.out.printf("Average time: %.2f ms%n", avgTimeMs);
        return avgTimeMs;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public double[][] getData() {
        double[][] copy = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

}

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        Matrix first = null;
        try {
            first = new Matrix(firstMatrix);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while creating first matrix: " + e.getMessage());
            System.exit(1);
        }

        Matrix second = null;
        try {
            second = new Matrix(secondMatrix);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while creating second matrix: " + e.getMessage());
            System.exit(1);
        }

        double[][] result = null;
        try {
            result = first.multiply(second);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while multiplying matrices: " + e.getMessage());
            System.exit(1);
        }

        return result;
    }

    public static void main(String[] args) {
        Matrix.measureMultiplicationPerformance(1000, 10);
    }

}
