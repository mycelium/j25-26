public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException("Неверные размеры матриц для умножения");
        }

        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        double[][] resultMatrix = new double[m][p];
        int blockSize = 256;

        for (int i = 0; i < m; i += blockSize) {
            for (int j = 0; j < p; j += blockSize) {
                for (int k = 0; k < n; k += blockSize) {
                    for (int bi = i; bi < Math.min(i + blockSize, m); bi++) {
                        for (int bj = j; bj < Math.min(j + blockSize, p); bj++) {
                            double sum = 0;
                            for (int bk = k; bk < Math.min(k + blockSize, n); bk++) {
                                sum += firstMatrix[bi][bk] * secondMatrix[bk][bj];
                            }
                            resultMatrix[bi][bj] += sum;
                        }
                    }
                }
            }
        }
        return resultMatrix;
    }

    public static double[][] naiveMultiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException("Неверные размеры матриц для умножения.");
        }

        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        double[][] resultMatrix = new double[m][p];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                for (int k = 0; k < n; k++) {
                    resultMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return resultMatrix;
    }

    public static void main(String[] args) {
        int[] sizes = {500, 1000, 1500};

        for (int size : sizes) {

            double[][] firstMatrix = new double[size][size];
            double[][] secondMatrix = new double[size][size];

     
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    firstMatrix[i][j] = Math.random();
                    secondMatrix[i][j] = Math.random();
                }
            }

            
            long startTimeNaive = System.nanoTime();
            double[][] naiveResult = naiveMultiply(firstMatrix, secondMatrix);
            long endTimeNaive = System.nanoTime();
            double naiveDuration = (endTimeNaive - startTimeNaive) / 1e6;

           
           
            long startTimeBlock = System.nanoTime();
            double[][] blockResult = multiply(firstMatrix, secondMatrix);
            long endTimeBlock = System.nanoTime();
            double blockDuration = (endTimeBlock - startTimeBlock) / 1e6;
      
            
            System.out.printf("Размер: %dx%d, Обычный метод: %.2f мс, Улучшенный метод: %.2f мс\n", size, size, naiveDuration, blockDuration);
        }
    }
}

