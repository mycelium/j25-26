import java.util.stream.IntStream;


public class MatrixMult{

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int rows2 = secondMatrix.length;
        int cols2 = secondMatrix[0].length;

        if (cols1 != rows2) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " +
                    cols1 + " != " + rows2);
        }

        double[][] result = new double[rows1][cols2];

        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                for (int k = 0; k < cols1; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] multiplyLevelUp(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int rows2 = secondMatrix.length;
        int cols2 = secondMatrix[0].length;

        if (cols1 != rows2) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " +
                    cols1 + " != " + rows2);
        }

        double[][] result = new double[rows1][cols2];
        double[][] secondMatrixTransposed = transpose(secondMatrix);

        IntStream.range(0, rows1).parallel().forEach(i -> {
            for (int j = 0; j < cols2; j++) {
                double sum = 0;
                for (int k = 0; k < cols1; k++) {
                    sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                }
                result[i][j] = sum;
            }
        });

        return result;
    }

    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }


    public static void main(String[] args) {
        int[] sizes = {500, 1000, 1500};

        for (int size : sizes) {
            System.out.println("\n=======Тест для матриц " + size + "x" + size + " =======");

            double[][] matrix1 = createRandomMatrix(size, size);
            double[][] matrix2 = createRandomMatrix(size, size);


            long startTime = System.nanoTime();
            double[][] result = multiplyLevelUp(matrix1, matrix2);
            long endTime = System.nanoTime();

            double durationMs = (endTime - startTime) / 1000000.0;
            System.out.println("Время выполнения: " +
                    String.format("%.2f", durationMs) + " мс");
        }
    }

    private static double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }



}

