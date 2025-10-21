public class MatrixMult {

    private static void validateMatrices(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Ошибка: передан null");
        }
        
        if (firstMatrix.length == 0 || firstMatrix[0].length == 0 || 
            secondMatrix.length == 0 || secondMatrix[0].length == 0) {
            throw new IllegalArgumentException("Ошибка: матрицы пустые");
        }
        
        if (firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException(
                "Несовместимые размеры: " + firstMatrix[0].length + " != " + secondMatrix.length
            );
        }
    }
    
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrices(firstMatrix, secondMatrix);
        
        int aRows = firstMatrix.length;
        int aCols = firstMatrix[0].length;
        int bCols = secondMatrix[0].length;
        
        double[][] result = new double[aRows][bCols];
        
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bCols; j++) {
                for (int k = 0; k < aCols; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        
        return result;
    }
    
    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        validateMatrices(firstMatrix, secondMatrix);
        
        int n = firstMatrix.length;
        int m = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        double[][] result = new double[n][p];
        
        for (int i = 0; i < n; i++) {
            double[] firstRow = firstMatrix[i];
            for (int k = 0; k < m; k++) {
                double value = firstRow[k];
                double[] secondRow = secondMatrix[k];
                for (int j = 0; j < p; j++) {
                    result[i][j] += value * secondRow[j];
                }
            }
        }
        
        return result;
    }

    public static double[][] multiplyLocalVars(double[][] A, double[][] B) {
        validateMatrices(A, B);
        
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;
        
        double[][] C = new double[n][p];
        
        for (int i = 0; i < n; i++) {
            double[] Ai = A[i];
            double[] Ci = C[i];
            for (int k = 0; k < m; k++) {
                double Aik = Ai[k];
                double[] Bk = B[k];
                for (int j = 0; j < p; j++) {
                    Ci[j] += Aik * Bk[j];
                }
            }
        }
        return C;
    }

    public static double[][] multiplyTransposed(double[][] A, double[][] B) {
        validateMatrices(A, B);
        
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;
        
        double[][] BT = new double[p][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                BT[j][i] = B[i][j];
            }
        }
        
        double[][] C = new double[n][p];
        
        for (int i = 0; i < n; i++) {
            double[] Ai = A[i];
            for (int j = 0; j < p; j++) {
                double sum = 0.0;
                double[] BTj = BT[j];
                for (int k = 0; k < m; k++) {
                    sum += Ai[k] * BTj[k];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }
    
    public static double[][] generateMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }
    
    public static void main(String[] args) {
        int[] sizes = {500, 1000, 1500};
        
        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Сравнение методов оптимизации для матриц " + size + "x" + size);
            System.out.println("=".repeat(50));

            double[][] m1 = generateMatrix(size);
            double[][] m2 = generateMatrix(size);
        
            long time;
        
            time = measureTime(() -> multiply(m1, m2));
            System.out.println("Базовый:              " + time + " мс");
        
            time = measureTime(() -> multiplyOptimized(m1, m2));
            System.out.println("Кэширование строк:    " + time + " мс");
        
            time = measureTime(() -> multiplyLocalVars(m1, m2));
            System.out.println("Локальные переменные: " + time + " мс");
        
            time = measureTime(() -> multiplyTransposed(m1, m2));
            System.out.println("С транспонированием:  " + time + " мс");
        }
    }
    
    private static long measureTime(Runnable operation) {
        long start = System.currentTimeMillis();
        operation.run();
        return System.currentTimeMillis() - start;
    }
}