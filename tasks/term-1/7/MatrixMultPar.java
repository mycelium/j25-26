public class MatrixMult {

    public static void printSystemInfo() {        
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());        
    }


    static class MatrixMultiplierThread extends Thread {
        private double[][] firstMatrix;
        private double[][] secondMatrixT;
        private double[][] result;
        private int startRow;
        private int endRow;
        private int colsB;
        private int colsA;

        public MatrixMultiplierThread(double[][] firstMatrix, double[][] secondMatrixT, 
                                    double[][] result, int startRow, int endRow, 
                                    int colsB, int colsA) {
            this.firstMatrix = firstMatrix;
            this.secondMatrixT = secondMatrixT;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
            this.colsB = colsB;
            this.colsA = colsA;
        }

        @Override
        public void run() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < colsB; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < colsA; k++) {
                        sum += firstMatrix[i][k] * secondMatrixT[j][k];
                    }
                    result[i][j] = sum;
                }
            }
        }
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
        	throw new IllegalArgumentException("Матрицы не могут быть null");
        }

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + colsA + " != " + rowsB
            );
        }

        double[][] result = new double[rowsA][colsB];
        
        int numThreads = getOptimalThreadCount(rowsA, colsB);
        System.out.println("Using " + numThreads + " threads for parallel computation");

        double[][] secondMatrixT = new double[colsB][rowsB];
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                secondMatrixT[j][i] = secondMatrix[i][j];
            }
        }

        Thread[] threads = new Thread[numThreads];
        int chunkSize = (rowsA + numThreads - 1) / numThreads;

        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int startRow = threadId * chunkSize;
            final int endRow = Math.min(startRow + chunkSize, rowsA);

            threads[threadId] = new MatrixMultiplierThread(
                firstMatrix, secondMatrixT, result, startRow, endRow, colsB, colsA
            );
            threads[threadId].start();
        }

        for (int threadId = 0; threadId < numThreads; threadId++) {
            try {
                threads[threadId].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Computation not finished", e);
            }
        }

        return result;
    }

    //оптимальное количество потоков
    public static int getOptimalThreadCount(int rows, int cols) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int matrixSize = Math.max(rows, cols);

        if (matrixSize < 100) {
            return 1;
        }

        else if (matrixSize < 300) {
            return Math.max(2, availableProcessors / 4);
        }
        
        else if (matrixSize < 800) {
            return Math.max(4, availableProcessors / 2);
        }
        
        else {
            return availableProcessors;
        }
        
    }

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
    	if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + colsA + " != " + rowsB
            );
        }
        
        double[][] result = new double[rowsA][colsB];
        
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                result[i][j] = sum;
            }
        }
        
        return result;
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
    	if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + colsA + " != " + rowsB
            );
        }
        
        double[][] result = new double[rowsA][colsB];
        
        double[][] secondMatrixT = new double[colsB][rowsB];
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                secondMatrixT[j][i] = secondMatrix[i][j];
            }
        }
        
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += firstMatrix[i][k] * secondMatrixT[j][k];
                }
                result[i][j] = sum;
            }
        }
        
        return result;
    }

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    public static boolean matricesEqual(double[][] a, double[][] b, double epsilon) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void testPerformance() {
        int[] sizes = {100, 200, 500, 1000, 2000};
        
        
        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Testing " + size + "x" + size + " matrices");
            System.out.println("=".repeat(50));
            
            double[][] matrixA = generateRandomMatrix(size, size);
            double[][] matrixB = generateRandomMatrix(size, size);

            long startTime = System.currentTimeMillis();
            double[][] resultSeq = multiplyOptimized(matrixA, matrixB);
            long endTime = System.currentTimeMillis();
            long seqTime = endTime - startTime;
            System.out.println("Sequential time: " + seqTime + " ms");

            startTime = System.currentTimeMillis();
            double[][] resultPar = multiplyParallel(matrixA, matrixB);
            endTime = System.currentTimeMillis();
            long parTime = endTime - startTime;
            System.out.println("Parallel time: " + parTime + " ms");
            
            boolean correct = matricesEqual(resultSeq, resultPar, 1e-10);
            System.out.println("Results are equal: " + correct);
            
            if (seqTime > 0 && parTime > 0) {
                double speedup = (double) seqTime / parTime;
                System.out.printf("Speedup: %.2fx\n", speedup);
                
                if (speedup > 1.0) {
                    System.out.printf("Parallel version is %.1f%% faster\n", (speedup - 1.0) * 100);
                } else {
                    System.out.printf("Sequential version is %.1f%% faster\n", (1.0 - speedup) * 100);
                }
            }
        }
        
    }

    public static void main(String[] args) {
        printSystemInfo();
        testPerformance();
    }
}
