public class MatrixMultPar {
    
    private static double[][] firstMatrix;
    private static double[][] secondMatrix;
    private static int threadCountMax = Runtime.getRuntime().availableProcessors();
    private static double[][] result;

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int numRowsFirst = firstMatrix.length;
        int numRowSecond = secondMatrix.length;
        int numColsFirst = firstMatrix[0].length;
        int numColsSecond = secondMatrix[0].length;

        if (numRowSecond != numColsFirst) {
            throw new IllegalArgumentException("Incorrect matrix sizes");
        }

        MatrixMultPar.firstMatrix = firstMatrix;
        MatrixMultPar.secondMatrix = secondMatrix;
        MatrixMultPar.result = new double[numRowsFirst][numColsSecond];
        
        int threadCount = Math.max(1, Math.min(numRowsFirst, threadCountMax));
        Thread[] threads = new Thread[threadCount];
        int rowsPerThread = (numRowsFirst + threadCount - 1) / threadCount;
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            final int startRow = threadId * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, numRowsFirst);
            
            threads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    multiplyRows(startRow, endRow);
                }
            });
            threads[t].start();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }
        
        return MatrixMultPar.result;
    }


    private static void multiplyRows(int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            for (int k = 0; k < secondMatrix.length; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < secondMatrix[0].length; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }
    }

    public static int findOptimalThreadCount(double[][] matrixA, double[][] matrixB) {        
        int maxThreadsToTest = threadCountMax * 2;
        int measurements = 10;
        long[] bestTimes = new long[maxThreadsToTest + 1];
        
        System.out.println("\nTest:");
        int originalThreadCountMax = threadCountMax;
        
        for (int threads = 1; threads <= maxThreadsToTest; threads++) {
            long totalTime = 0;
            
            for (int i = 0; i < measurements; i++) {
                threadCountMax = threads;
                long startTime = System.currentTimeMillis();
                multiplyParallel(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            
            long avgTime = totalTime / measurements;
            bestTimes[threads] = avgTime;
         
        }
        
        threadCountMax = originalThreadCountMax;
        int optimalThreads = 1;
        long minTime = bestTimes[1];
        for (int threads = 2; threads <= maxThreadsToTest; threads++) {
            if (bestTimes[threads] < minTime) {
                minTime = bestTimes[threads];
                optimalThreads = threads;
            }
        }
        return optimalThreads;
    }

    public static double[][] generateRandomMatrix(int rows, int cols, int min, int max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%8.2f", matrix[i][j]);
            }
            System.out.println();
        }
    }

    public static void testSmallMatrix() {
        double[][] A = {
            {1, 0, 3},
            {2, 5, 7}
        };

        double[][] B = {
            {6, 5},
            {3, 15},
            {2, 1}
        };

        System.out.println("Testing parallel multiplication on small matrix:");
        double[][] C = multiplyParallel(A, B);
        System.out.println("Result: ");
        printMatrix(C);
        
      
    }

    public static void simpleTestLargeMatrix(){
	 int[][] sizes = {
            {500, 500, 500},
            {1000, 1000, 1000},
			{1500, 1500, 1500},
        };

         for (int[] size : sizes) {
            int rowsA = size[0];
            int colsA = size[1];
            int colsB = size[2];

            double[][] matrixA = generateRandomMatrix(rowsA, colsA,1,100);
            double[][] matrixB = generateRandomMatrix(colsA, colsB,1,100);
            
			long totalTime=0;
			for (int i = 0; i < 15; i++){
            long startTime = System.currentTimeMillis();
            double[][] result = multiplyParallel(matrixA, matrixB);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime+=duration;
            }
			System.out.println("Result for simpleTestLargeMatrix: ");
			System.out.println("Threads used: " + Math.max(1, Math.min(matrixA.length, threadCountMax)));
		    System.out.println("Average execution time for matrix sizes"+(rowsA)+"x" +(colsA)+ " and "+ (colsA)+"x" +(colsB)+": " + (totalTime / 15) + " ms");
		}

    }

    public static void testLargeMatrix(){
	 int[][] sizes = {
            {500, 500, 500},
            {1000, 1000, 1000},
			{1500, 1500, 1500},
        };

         for (int[] size : sizes) {
            int rowsA = size[0];
            int colsA = size[1];
            int colsB = size[2];

            double[][] matrixA = generateRandomMatrix(rowsA, colsA,1,100);
            double[][] matrixB = generateRandomMatrix(colsA, colsB,1,100);
            int originalThreadCountMax = threadCountMax;
            threadCountMax=findOptimalThreadCount(matrixA,matrixB);
			long totalTime=0;
			for (int i = 0; i < 15; i++){
            long startTime = System.currentTimeMillis();
            double[][] result = multiplyParallel(matrixA, matrixB);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime+=duration;
            }
			System.out.println("Result for testLargeMatrix: ");
			System.out.println("Threads used: " + threadCountMax);
			threadCountMax=originalThreadCountMax;
		    System.out.println("Average execution time for matrix sizes "+(rowsA)+"x" +(colsA)+ " and "+ (colsA)+"x" +(colsB)+": " + (totalTime / 15) + " ms");
		}

}
    
   public static void main(String[] args) {
        
        //testSmallMatrix(); //Проверка на корректность выплонения перемножения матриц, на примере небольших матриц
	    simpleTestLargeMatrix(); //Параллельное перемножение больших матриц 
		//testLargeMatrix();  // Параллельное перемножение больших матриц с учетом вычисления оптимального количества потоков в момент выполнении программы (данный запуск займет больше всего времени,т.к будет выполняться поиск оптимального времени)
    }
}