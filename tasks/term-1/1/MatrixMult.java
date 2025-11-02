public class MatrixMult {

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] resMatrix = new double[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                resMatrix[y][x] = Math.random();
            }
        }
        return resMatrix;
    }

    public static double[][] multiply(double[][] oneMatrix, double[][] twoMatrix) {
        int rows1 = oneMatrix.length;
        int cols1 = oneMatrix[0].length;
        int rows2 = twoMatrix.length;
        int cols2 = twoMatrix[0].length;

        if (cols1 != rows2) {
            throw new IllegalArgumentException(
                    "невозможно выполнить умножение, так как размеры матриц несовместимы: " + cols1 + " != " + rows2
            );
        }

        double[][] result = new double[rows1][cols2];
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                double sum = 0.0;
                for (int k = 0; k < cols1; k++) {
                    sum += oneMatrix[i][k] * twoMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    private static final int BLOCK_SIZE = 128;

    public static double[][] blockMultiply(double[][] oneMatrix, double[][] twoMatrix) {
        int rows1 = oneMatrix.length;
        int cols1 = oneMatrix[0].length;
        int rows2 = twoMatrix.length;
        int cols2 = twoMatrix[0].length;

        if (cols1 != twoMatrix.length) {
            throw new IllegalArgumentException("невозможно выполнить умножение, так как размеры" +
                    " матриц несовместимы: " + cols1 + " != " + rows2);
        }

        double[][] result = new double[rows1][cols2];
        long operations = (long) rows1 * cols1 * cols2;

        if (operations > 50_000_000L) {
            for (int i0 = 0; i0 < rows1; i0 += BLOCK_SIZE) {
                int i1 = Math.min(i0 + BLOCK_SIZE, rows1);
                for (int k0 = 0; k0 < cols1; k0 += BLOCK_SIZE) {
                    int k1 = Math.min(k0 + BLOCK_SIZE, cols1);
                    for (int j0 = 0; j0 < cols2; j0 += BLOCK_SIZE) {
                        int j1 = Math.min(j0 + BLOCK_SIZE, cols2);
                        for (int i = i0; i < i1; i++) {
                            for (int k = k0; k < k1; k++) {
                                double a = oneMatrix[i][k];
                                for (int j = j0; j < j1; j++) {
                                    result[i][j] += a * twoMatrix[k][j];
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < rows1; i++) {
                for (int k = 0; k < cols1; k++) {
                    double a = oneMatrix[i][k];
                    for (int j = 0; j < cols2; j++) {
                        result[i][j] += a * twoMatrix[k][j];
                    }
                }
            }
        }
        return result;
    }


    public static void main(String[] args) {
        long sum_standart = 0;
        long sum_opt = 0;
        int count = 10;
        for (int i = 0; i < count; i++) {
            double[][] A = generateRandomMatrix(2000, 2000);
            double[][] B = generateRandomMatrix(2000, 2000);

            long start = System.currentTimeMillis();
            multiply(A, B);
            sum_standart += System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            blockMultiply(A, B);
            sum_opt += System.currentTimeMillis() - start;
        }
        System.out.println("cреднее время базового умножения матриц для 10 тестов: " + (sum_standart / count));
        System.out.println("среднее время блочного умножения матриц для 10 тестов: " + (sum_opt / count));
    }

}
