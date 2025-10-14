import java.util.Random;

class MatrixMultiplier {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        if (n != secondMatrix.length) {
            System.out.println("несовместимые размеры матриц");
            return new double[0][0];
        }

        double[][] result = new double[m][p];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        if (n != secondMatrix.length) {
            System.out.println("несовместимые размеры матриц");
            return new double[0][0];
        }

        double[][] secondMatrixT = transpose(secondMatrix);

        double[][] result = new double[m][p];

        for (int i = 0; i < m; i++) {
            double[] firstRow = firstMatrix[i];
            double[] resultRow = result[i];

            for (int j = 0; j < p; j++) {
                double sum = 0;
                double[] secondRowT = secondMatrixT[j];

                for (int k = 0; k < n; k++) {
                    sum += firstRow[k] * secondRowT[k];
                }
                resultRow[j] = sum;
            }
        }

        return result;
    }

    private static double[][] transpose(double[][] matrix) {
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

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble() * 100;
            }
        }

        return matrix;
    }

    public static void printMatrix(double[][] matrix, int maxRows, int maxCols) {
        int rows = Math.min(matrix.length, maxRows);
        int cols = Math.min(matrix[0].length, maxCols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%8.2f", matrix[i][j]);
            }
            System.out.println("...");
        }
        System.out.println("...");
    }

    public static void main(String[] args) {
        int[] sizes = {500, 1000, 1500};

        System.out.println("умножение матриц");
        System.out.println("================");

        for (int size : sizes) {
            System.out.println("\n");
            System.out.println("размер матриц: " + size + "x" + size);

            double[][] A = generateRandomMatrix(size, size);
            double[][] B = generateRandomMatrix(size, size);
            
            long startTime = System.currentTimeMillis();
            double[][] result1 = multiply(A, B);
            long endTime = System.currentTimeMillis();
            long basicTime = endTime - startTime;

            startTime = System.currentTimeMillis();
            double[][] result2 = multiplyOptimized(A, B);
            endTime = System.currentTimeMillis();
            long optimizedTime = endTime - startTime;

            System.out.println("время выполнения (базовый): " + basicTime + " мс");
            System.out.println("время выполнения (оптимизированный): " + optimizedTime + " мс");
            System.out.println("ускорение: " + String.format("%.2f", (double)basicTime/optimizedTime) + " раз");
            System.out.println("размер результата: " + result1.length + "x" + result1[0].length);

            if (size <= 10) {
                System.out.println("первые 3x3 элемента результата:");
                printMatrix(result1, 3, 3);
            }
        }


        System.out.println("\n");
        System.out.println("тест с неквадратными матрицами:");
        System.out.println("===============================");

        double[][] A = generateRandomMatrix(200, 300);
        double[][] B = generateRandomMatrix(300, 400);

        long startTime = System.currentTimeMillis();
        double[][] result = multiplyOptimized(A, B);
        long endTime = System.currentTimeMillis();

        System.out.println("матрица A: 200x300");
        System.out.println("матрица B: 300x400");
        System.out.println("результат: " + result.length + "x" + result[0].length);
        System.out.println("время выполнения: " + (endTime - startTime) + " мс");
    }
}
