public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int cols2 = secondMatrix[0].length;

        double[][] result = new double[rows1][cols2];

        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                double sum = 0;
                for (int k = 0; k < cols1; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }
    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int cols2 = secondMatrix[0].length;

        double[][] result = new double[rows1][cols2];


        double[][] secondT = new double[cols2][cols1];
        for (int i = 0; i < cols1; i++) {
            for (int j = 0; j < cols2; j++) {
                secondT[j][i] = secondMatrix[i][j];
            }
        }
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                double sum = 0;
                for (int k = 0; k < cols1; k++) {
                    sum += firstMatrix[i][k] * secondT[j][k];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int size = 1000;
        double[][] A = new double[size][size];
        double[][] B = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A[i][j] = Math.random();
                B[i][j] = Math.random();
            }
        }

        System.out.println("Умножение обычным методом...");
        long start = System.currentTimeMillis();
        double[][] result1 = multiply(A, B);
        long end = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (end - start) + " мс");

        System.out.println("Умножение оптимизированным методом...");
        start = System.currentTimeMillis();
        double[][] result2 = multiplyOptimized(A, B);
        end = System.currentTimeMillis();
        System.out.println("Время выполнения (оптимизация): " + (end - start) + " мс");
    }
}