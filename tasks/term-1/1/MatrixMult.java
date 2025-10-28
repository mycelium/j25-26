public class MatrixMult {

    //умножение матриц с оптимизацией по порядку ikj
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;

        double[][] result = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < p; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < m; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }

        return result;
    }

    // генерация случайной матрицы
    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        int size = 1000; // размерность большой матрицы
        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);

        long start = System.nanoTime();
        double[][] C = multiply(A, B);
        long end = System.nanoTime();

        double timeSeconds = (end - start) / 1e9;
        System.out.printf("Время выполнения умножения матриц размером %dx%d: %.3f секунд%n",
                size, size, timeSeconds);
    }
}

