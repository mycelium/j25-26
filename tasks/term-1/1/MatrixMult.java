public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Matrices must not be null");
        }

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                    "Matrices can't be multiplied: columns of A (" + colsA +
                            ") != rows of B (" + rowsB + ")"
            );
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            double[] rowA = firstMatrix[i];
            double[] rowResult = result[i];

            for (int k = 0; k < colsA; k++) {
                double valueA = rowA[k];
                double[] rowB = secondMatrix[k];

                for (int j = 0; j < colsB; j++) {
                    rowResult[j] += valueA * rowB[j];
                }
            }
        }

        return result;
    }

    public static double[][] createRandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        double range = max - min;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = min + Math.random() * range;
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix, String name) {
        if (matrix == null) {
            System.out.println(name + ": Matrix is empty");
            return;
        }

        System.out.println("\n" + name + " (" + matrix.length + "x" + matrix[0].length + ")");
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%.3f ", value);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int size = 2000;
        int experiments = 5;

        double[][] matrixA = createRandomMatrix(size, size, 1, 100);
        double[][] matrixB = createRandomMatrix(size, size, 1, 100);

        long totalTime = 0;

        for (int i = 1; i <= experiments; i++) {
            long start = System.nanoTime();
            multiply(matrixA, matrixB);
            long end = System.nanoTime();

            long durationMs = (end - start) / 1_000_000;
            totalTime += durationMs;

            System.out.println("Iteration " + i + ": " + durationMs + " ms");
        }

        System.out.println("\n=== RESULTS ===");
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time: " + (totalTime / experiments) + " ms");
    }
}
