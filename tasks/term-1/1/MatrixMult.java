public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " + colsA + " столбцов != " + rowsB + " строк");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " + colsA + " столбцов != " + rowsB + " строк");
        }

        double[][] secondMatrixTransposed = transpose(secondMatrix);
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }

        return result;
    }

    public static double[][] createRandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = min + Math.random() * (max - min);
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix) {
        if (matrix == null) {
            System.out.println("Матрица: null");
            return;
        }

        System.out.println("Матрица " + matrix.length + "x" + matrix[0].length + ":");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%8.2f ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        double[][] B = {{7, 8}, {9, 10}, {11, 12}};

        System.out.println("Матрица A:");
        printMatrix(A);

        System.out.println("Матрица B:");
        printMatrix(B);

        System.out.println("Стандартное умножение:");
        double[][] result1 = multiply(A, B);
        printMatrix(result1);

        System.out.println("Оптимизированное умножение:");
        double[][] result2 = multiplyOptimized(A, B);
        printMatrix(result2);

        int size = 500;
        int iterations = 5;

        System.out.println("Создание матриц " + size + "x" + size + "...");
        double[][] largeA = createRandomMatrix(size, size, 0, 10);
        double[][] largeB = createRandomMatrix(size, size, 0, 10);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            multiply(largeA, largeB);
        }
        long standardTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            multiplyOptimized(largeA, largeB);
        }
        long optimizedTime = System.currentTimeMillis() - startTime;

        System.out.println("Среднее время стандартного метода: " + (standardTime / (double)iterations) + " мс");
        System.out.println("Среднее время оптимизированного метода: " + (optimizedTime / (double)iterations) + " мс");
        System.out.println("Отношение скоростей: " + (standardTime / (double)optimizedTime));
    }
}