import java.util.Random;

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int p = firstMatrix[0].length;
        int p2 = secondMatrix.length;
        int n = secondMatrix[0].length;
        if (p != p2) {
            throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
        }
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < p; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                res[i][j] = sum;
            }
        }
        return res;
    }
    public static double[][] multiplypro(double[][] A, double[][] B) {
        int m = A.length;
        if (m == 0) return new double[0][0];
        int p = A[0].length;
        int p2 = B.length;
        int n = B[0].length;
        if (p != p2) {
            throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
        }
        double[][] C = new double[m][n];
        final int blockSize = 64;
        for (int ii = 0; ii < m; ii += blockSize) {
            int iMax = Math.min(ii + blockSize, m);
            for (int kk = 0; kk < p; kk += blockSize) {
                int kMax = Math.min(kk + blockSize, p);
                for (int jj = 0; jj < n; jj += blockSize) {
                    int jMax = Math.min(jj + blockSize, n);

                    for (int i = ii; i < iMax; i++) {
                        double[] aRow = A[i];
                        double[] cRow = C[i];
                        for (int k = kk; k < kMax; k++) {
                            double aVal = aRow[k];
                            double[] bRow = B[k];
                            for (int j = jj; j < jMax; j++) {
                                cRow[j] += aVal * bRow[j];
                            }
                        }
                    }

                }
            }
        }
        return C;
    }

    public static double[][] createMatrix(int rows, int cols, double min, double max) {
        Random rnd = new Random();
        double[][] a = new double[rows][cols];
        double range = max - min;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a[i][j] = min + rnd.nextDouble() * range;
            }
        }
        return a;
    }
    public static void main(String[] args) {
        int[] sizes = {50, 100, 500, 1000};
        int repetitions = 10;

        for (int size : sizes) {
            System.out.println("Size: " + size + "x" + size);

            long totalTimeNsMul = 0L;
            long totalTimeNsMulPro = 0L;

            for (int run = 0; run < repetitions; run++) {

                double[][] m1 = createMatrix(size, size, 0.0, 10.0);
                double[][] m2 = createMatrix(size, size, 0.0, 10.0);

                long start1 = System.nanoTime();
                double[][] res1 = multiply(m1, m2);
                long end1 = System.nanoTime();
                totalTimeNsMul += (end1 - start1);

                long start2 = System.nanoTime();
                double[][] res2 = multiplypro(m1, m2);
                long end2 = System.nanoTime();
                totalTimeNsMulPro += (end2 - start2);

            }

            double avgMulMs = totalTimeNsMul / (double) repetitions / 1_000_000.0;
            double avgMulProMs = totalTimeNsMulPro / (double) repetitions / 1_000_000.0;

            System.out.printf("Average multiply: %.3f ms\n", avgMulMs);
            System.out.printf("Average multiplypro: %.3f ms\n\n", avgMulProMs);
        }
    }
}
