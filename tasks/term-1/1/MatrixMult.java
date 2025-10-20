import java.util.Random;

public class MatrixMult {

    private static final int BLOCK_SIZE = 128;

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];
        long operations = (long) rowsA * colsA * colsB;

        if (operations > 50_000_000L) {
            // Блочное умножение
            for (int i0 = 0; i0 < rowsA; i0 += BLOCK_SIZE) {
                int i1 = Math.min(i0 + BLOCK_SIZE, rowsA);
                for (int k0 = 0; k0 < colsA; k0 += BLOCK_SIZE) {
                    int k1 = Math.min(k0 + BLOCK_SIZE, colsA);
                    for (int j0 = 0; j0 < colsB; j0 += BLOCK_SIZE) {
                        int j1 = Math.min(j0 + BLOCK_SIZE, colsB);
                        for (int i = i0; i < i1; i++) {
                            for (int k = k0; k < k1; k++) {
                                double a = firstMatrix[i][k];
                                for (int j = j0; j < j1; j++) {
                                    result[i][j] += a * secondMatrix[k][j];
                                }
                            }
                        }
                    }
                }
            }
        } else {
            //  i → k → j
            for (int i = 0; i < rowsA; i++) {
                for (int k = 0; k < colsA; k++) {
                    double a = firstMatrix[i][k];
                    for (int j = 0; j < colsB; j++) {
                        result[i][j] += a * secondMatrix[k][j];
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] sizes = {100, 1000, 2000}; 
        int iterations = 20;
        Random rand = new Random(0); 

        for (int size : sizes) {
            System.out.println("\nТест для матриц " + size + "x" + size );

            // Генерация матриц
            double[][] A = new double[size][size];
            double[][] B = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    A[i][j] = rand.nextDouble() * 10;
                    B[i][j] = rand.nextDouble() * 10;
                }
            }

            
            long totalTime = 0;
            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();
                multiply(A, B);
                long end = System.currentTimeMillis();
                totalTime += (end - start);
            }

            double avgTimeMs = (double) totalTime / iterations;
            System.out.printf("Время умножения матриц %dx%d за %d итераций: %.2f мс%n",
        size, size, iterations, avgTimeMs);
        }
    }
}
