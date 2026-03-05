public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int rows2 = secondMatrix.length;
        int cols2 = secondMatrix[0].length;

        if (cols1 != rows2) {
            throw new IllegalArgumentException("Matrix dimensions do not match.");
        }

        double[][] result = new double[rows1][cols2];

        double[][] secondT = new double[cols2][rows2];
        for (int i = 0; i < rows2; i++) {
            for (int j = 0; j < cols2; j++) {
                secondT[j][i] = secondMatrix[i][j];
            }
        }

        for (int i = 0; i < rows1; i++) {
            double[] rowA = firstMatrix[i];
            for (int j = 0; j < cols2; j++) {
                double[] rowB = secondT[j];
                double sum = 0.0;
                for (int k = 0; k < cols1; k++) {
                    sum += rowA[k] * rowB[k];
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

        System.out.println("Matrix multiply " + size + "x" + size + "...");

        long start = System.nanoTime();
        double[][] result = multiply(A, B);
        long end = System.nanoTime();

        double elapsed = (end - start) / 1_000_000_000.0;
        System.out.printf("Execution time: %.6f seconds%n", elapsed);
    }
}
