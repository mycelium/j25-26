public class MatrixMult {

    private static int blockSize = 64;

    public static void main(String[] args) throws IllegalArgumentException {

        int[] sizes = {500,1000,2000}; 

        for (int size : sizes) {
            System.out.printf("--------------------\nSize: %d\n\n", size);
            double[][] A = new double[size][size]; 
            double[][] B = new double[size][size]; 
			
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++) {
                    A[i][j] = Math.random() * 99;
                    B[i][j] = Math.random() * 99;
                }

            long start = System.currentTimeMillis();
            multiply(A, B);
            long notOpt = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            multiplyOpt(A, B);
            long opt = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            multiplyTrans(A, B);
            long trans = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            multiplyBlock(A, B);
            long block = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            multiplyOptBlock(A, B);
            long optBlock = System.currentTimeMillis() - start;

            System.out.printf("Not opt = %d\nOpt = %d\nTrans = %d\nBlock = %d\nOptBlock = %d\n",
                               notOpt, opt, trans, block, optBlock);
        }
    }

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {

        // Исключения
        validateMatrixes(firstMatrix, secondMatrix);
        // Переменные
        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;
        double[][] res = new double[n][m];
        // Вычисление
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                for (int k = 0; k < p; k++)
                    res[i][j] += firstMatrix[i][k] * secondMatrix[k][j];

        return res;
    }

    public static double[][] multiplyTrans(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {

        // Исключения
        validateMatrixes(firstMatrix, secondMatrix);
        // Переменные
        double[][] tmpM = transpon(secondMatrix);
        int n = firstMatrix.length;
        int m = tmpM.length;
        int p = tmpM[0].length;
        double[][] res = new double[n][m];
        // Вычисление
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = 0; k < p; k++)
                    sum += firstMatrix[i][k] * tmpM[j][k];
                res[i][j] = sum;
            }
        return res;
    }

    public static double[][] multiplyOpt(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {

        // Исключения
        validateMatrixes(firstMatrix, secondMatrix);
        // Переменные
        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;
        double[][] res = new double[n][m];
        // Вычисление
        for (int i = 0; i < n; i++)
            for (int k = 0; k < p; k++) {
                double firstVal = firstMatrix[i][k];
                for (int j = 0; j < m; j++)
                    res[i][j] += firstVal * secondMatrix[k][j];
            }
        return res;
    }

    public static double[][] multiplyBlock(double[][] firstMatrix, double[][] secondMatrix) {

        // Исключения
        validateMatrixes(firstMatrix, secondMatrix);
        // Переменные
        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;
        double[][] res = new double[n][m];
        // Вычисление
        for (int i0 = 0; i0 < n; i0 += blockSize)
            for (int j0 = 0; j0 < m; j0 += blockSize)
                for (int k0 = 0; k0 < p; k0 += blockSize) {

                    int iBlockSize = Math.min(i0 + blockSize, n);
                    int jBlockSize = Math.min(j0 + blockSize, m);
                    int kBlockSize = Math.min(k0 + blockSize, p);

                    for (int i = i0; i < iBlockSize; i++)
                        for (int j = j0; j < jBlockSize; j++)
                            for (int k = k0; k < kBlockSize; k++)
                                res[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
        return res;
    }

    public static double[][] multiplyOptBlock(double[][] firstMatrix, double[][] secondMatrix) {

        // Исключения
        validateMatrixes(firstMatrix, secondMatrix);
        // Переменные
        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;
        double[][] res = new double[n][m];
        // Вычисление
        for (int i0 = 0; i0 < n; i0 += blockSize)
            for (int k0 = 0; k0 < p; k0 += blockSize)
                for (int j0 = 0; j0 < m; j0 += blockSize) {

                    int iBlockSize = Math.min(i0 + blockSize, n);
                    int jBlockSize = Math.min(j0 + blockSize, m);
                    int kBlockSize = Math.min(k0 + blockSize, p);

                    for (int i = i0; i < iBlockSize; i++)
                        for (int k = k0; k < kBlockSize; k++) {
                            double firstVal = firstMatrix[i][k];
                            for (int j = j0; j < jBlockSize; j++)
                                res[i][j] += firstVal * secondMatrix[k][j];
                        }
                }
        return res;
    }

    private static double[][] transpon(double[][] matrx) {

        int n = matrx.length, m = matrx[0].length;
        double[][] res = new double[m][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                res[j][i] = matrx[i][j];
        return res;
    }

    private static void validateMatrixes(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {
		
        if (firstMatrix == null || secondMatrix == null)
            throw new IllegalArgumentException("Argument cannot be null.");
        else if (hasDiffLengths(firstMatrix) || hasDiffLengths(secondMatrix))
            throw new IllegalArgumentException("One of the matrixes has rows with different amounts of elements.");
        else if (firstMatrix[0].length != secondMatrix.length)
            throw new IllegalArgumentException("Amount of columns in first matrix" +
                                               firstMatrix[0].length +
                                               " isn't the same as the amount of rows in the second one "
                                               + secondMatrix.length);
    }

    private static boolean hasDiffLengths(double[][] m) {
		
        if (m.length != 0) {
            int validLength = m[0].length;
            for (double[] row : m)
                if (row.length != validLength) return true;
        }
        return false;
    }
}
