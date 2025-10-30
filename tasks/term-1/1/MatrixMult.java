import java.util.List;

public class MatrixMult {
    private static final int DEFAULT_BLOCK_SIZE = 256;

    public static void main(String[] args) {
        record MatrixTest(int rows, int columns, int testCount) {}

        List<MatrixTest> tests = List.of(
            new MatrixTest(200, 200, 10),
            new MatrixTest(400, 600, 10),
            new MatrixTest(1000, 1000, 10),
            new MatrixTest(2000, 2000, 10),
            new MatrixTest(2500, 1042, 10),
            new MatrixTest(5000, 5000, 10)
        );

        for (MatrixTest test : tests) {
            long test_time = 0;
            long algorithm_test_time = 0;

            for (int i = 0; i < test.testCount(); i++) {
                double[][] firstMatrix = generateRandomMatrix(test.rows(), test.columns());
                double[][] secondMatrix = generateRandomMatrix(test.columns(), test.rows());

                long start = System.currentTimeMillis();
                multiply(firstMatrix, secondMatrix);
                long end = System.currentTimeMillis();
                test_time += (end - start);

                start = System.currentTimeMillis();
                multiplyBlock(firstMatrix, secondMatrix);
                end = System.currentTimeMillis();
                algorithm_test_time += (end - start);
            }
            System.out.printf("Среднее время стандартного умножения матрицы %dx%d за %d тестов: %d мс%n",
                    test.rows(), test.columns(), test.testCount(), test_time / test.testCount());

            System.out.printf("Среднее время умножения матрицы по методу блоков %dx%d за %d тестов: %d мс%n",
                    test.rows(), test.columns(), test.testCount(), algorithm_test_time / test.testCount());
        }
    }

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrix(firstMatrix, secondMatrix);

        int firstMatrixRows = firstMatrix.length;
        int firstMatrixColumns = firstMatrix[0].length;
        int secondMatrixColumns = secondMatrix[0].length;

        double[][] resultMatrix = new double[firstMatrixRows][secondMatrixColumns];

        for (int i = 0; i < firstMatrixRows; i++) {
            for (int k = 0; k < firstMatrixColumns; k++) {
                double cache = firstMatrix[i][k];
                for (int j = 0; j < secondMatrixColumns; j++) {
                    resultMatrix[i][j] += cache * secondMatrix[k][j];
                }
            }
        }

        return resultMatrix;
    }

    public static double[][] multiplyBlock(double[][] firstMatrix, double[][] secondMatrix) {
        return multiplyBlock(firstMatrix, secondMatrix, DEFAULT_BLOCK_SIZE);
    }

    public static double[][] multiplyBlock(double[][] firstMatrix, double[][] secondMatrix, int blockSize) {
        validateMatrix(firstMatrix, secondMatrix);

        int firstMatrixRows = firstMatrix.length;
        int firstMatrixColumns = firstMatrix[0].length;
        int secondMatrixColumns = secondMatrix[0].length;

        double[][] resultMatrix = new double[firstMatrixRows][secondMatrixColumns];

        for (int i0 = 0; i0 < firstMatrixRows; i0 += blockSize) {
            int iMax = Math.min(i0 + blockSize, firstMatrixRows);

            for (int k0 = 0; k0 < firstMatrixColumns; k0 += blockSize) {
                int kMax = Math.min(k0 + blockSize, firstMatrixColumns);

                for (int j0 = 0; j0 < secondMatrixColumns; j0 += blockSize) {
                    int jMax = Math.min(j0 + blockSize, secondMatrixColumns);

                    for (int i = i0; i < iMax; i++) {
                        for (int k = k0; k < kMax; k++) {
                            double aik = firstMatrix[i][k];
                            for (int j = j0; j < jMax; j++) {
                                resultMatrix[i][j] += aik * secondMatrix[k][j];
                            }
                        }
                    }
                }
            }
        }

        return resultMatrix;
    }

    private static void validateMatrix(double[][] firstMatrix, double[][] secondMatrix) {
        int firstMatrixColumns = firstMatrix[0].length;
        int secondMatrixRows = secondMatrix.length;

        if (firstMatrixColumns != secondMatrixRows) {
            throw new IllegalArgumentException("Невозможно умножить данные матрицы. " +
                    "Число столбцов первой матрицы (%d) неравно числу строк второй матрицы (%d)"
                            .formatted(firstMatrixColumns,  secondMatrixRows));
        }
    }

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }
}
