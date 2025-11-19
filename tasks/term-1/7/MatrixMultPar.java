public class MatrixMultPar {

    private static int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private static class MatrixMultiplierTask implements Runnable {
        private final double[][] firstMatrix;
        private final double[][] secondMatrix;
        private final double[][] result;
        private final int startRow;
        private final int endRow;

        public MatrixMultiplierTask(double[][] firstMatrix, double[][] secondMatrix, double[][] result, int startRow, int endRow) {
            this.firstMatrix = firstMatrix;
            this.secondMatrix = secondMatrix;
            this.result = result;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run() {
            int colsA = firstMatrix[0].length;
            int colsB = secondMatrix[0].length;
            
            for (int i = startRow; i < endRow; i++) {
                for (int k = 0; k < colsA; k++) {
                    double val = firstMatrix[i][k];
                    for (int j = 0; j < colsB; j++) {
                        result[i][j] += val * secondMatrix[k][j];
                    }
                }
            }
        }
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        try {
            if (colsA != rowsB) {
                throw new IllegalArgumentException(
                    "Matrices can't be multiplied!" + 
                    "The number of first matrix's columns (" + 
                    colsA + ") should match the number of second matrix's rows (" + rowsB + ")"
                );
            }
        } catch (IllegalArgumentException err) {
            System.out.println("Error: " + err.getMessage());
            return null;
        }

        double[][] result = new double[rowsA][colsB];
        Thread[] threads = new Thread[NUM_THREADS];
        
        int rowsPerThread = Math.max(1, rowsA / NUM_THREADS);
        
        for (int thread = 0; thread < NUM_THREADS; thread++) {
            final int startRow = thread * rowsPerThread;
            final int endRow = (thread == NUM_THREADS - 1) ? rowsA : (thread + 1) * rowsPerThread;
            
            Runnable task = new MatrixMultiplierTask(firstMatrix, secondMatrix, result, startRow, endRow);
            threads[thread] = new Thread(task);
            threads[thread].start();
        }

        for (int thread = 0; thread < NUM_THREADS; thread++) {
            try {
                threads[thread].join();
            } catch (InterruptedException e) {
                System.out.println("Thread execution interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return result;
    }

    public static double[][] createRandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix, String name) {
        if (matrix == null) {
            System.out.println(name + ": Matrix is empty");
            return;
        }
        
        System.out.println("\n" + name + " (" + matrix.length + "x" + matrix[0].length + "):");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%.3f ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    public static void setNumThreads(int numThreads) {
        NUM_THREADS = numThreads;
    }
    
    public static int getNumThreads() {
        return NUM_THREADS;
    }

    public static void findOptimalThreadCount(double[][] matrixA, double[][] matrixB, int numExperiments) {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        long bestTime = Long.MAX_VALUE;
        int optimalThreads = 1;
        
        System.out.println("Testing thread counts from 1 to " + maxThreads);
        
        for (int threads = 1; threads <= maxThreads; threads++) {
            setNumThreads(threads);
            long totalTime = 0;
            
            for (int exp = 0; exp < numExperiments; exp++) {
                long startTime = System.currentTimeMillis();
                multiplyParallel(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            
            long avgTime = totalTime / numExperiments;
            System.out.println("Threads: " + threads + ", Average time: " + avgTime + " ms");
            
            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = threads;
            }
        }

        System.out.println("\n---===[Optimal Configuration]===---");
        System.out.println("Optimal thread count: " + optimalThreads);
        System.out.println("Best average time: " + bestTime + " ms");
    }

    public static void main(String[] args) {
        int size = 1000;
        int numExperiments = 10;

        double[][] matrixA = createRandomMatrix(size, size, 1, 100);
        double[][] matrixB = createRandomMatrix(size, size, 1, 100);

        long totalTime = 0;

        for (int exp = 0; exp < numExperiments; exp++){
            long startTime = System.currentTimeMillis();
            multiplyParallel(matrixA, matrixB);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            System.out.println("Execution time for iteration " + (exp + 1) + ": " + duration + " ms");
        }

        System.out.println("---===[Results]===---");
        System.out.println("Total execution time for all multiplications: " + totalTime + " ms");
        System.out.println("Average execution time: " + (totalTime / numExperiments) + " ms");

        System.out.println("---===[Finding Optimal Thread Count]===---");
        findOptimalThreadCount(matrixA, matrixB, numExperiments);
    }
}