public class MatrixMultPar {

	private static int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {

        int firstRowCount = firstMatrix.length;
        int firstColCount = firstMatrix[0].length;
        int secondRowCount = secondMatrix.length;
        int secondColCount = secondMatrix[0].length;

        if (firstColCount != secondRowCount) {
            throw new IllegalArgumentException("Incorrect sizes for multiply");
        }

        double[][] resultMatrix = new double[firstRowCount][secondColCount];

        int threadCount = Math.min(NUM_THREADS, firstRowCount);
        Thread[] threads = new Thread[threadCount];

        int rowsPerThread = firstRowCount / threadCount;
        int remainingRows = firstRowCount % threadCount;

        int startRowIndex = 0;

        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {

            int rowsForCurrentThread =
                    rowsPerThread + (threadIndex < remainingRows ? 1 : 0);

            int fromRow = startRowIndex;
            int toRow = fromRow + rowsForCurrentThread;

            threads[threadIndex] = new Thread(() -> {

                for (int i = fromRow; i < toRow; i++) {
                    for (int k = 0; k < firstColCount; k++) {
                        double tempValue = firstMatrix[i][k];
                        for (int j = 0; j < secondColCount; j++) {
                            resultMatrix[i][j] += tempValue * secondMatrix[k][j];
                        }
                    }
                }

            });

            threads[threadIndex].start();
            startRowIndex = toRow;
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread execution interrupted", e);
            }
        }

        return resultMatrix;
    }

    public static double[][] generateMatrix(int rows, int cols) {
		int min = 10;
		int max = 100;
		double[][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = (Math.random() * (max - min + 1)) + min;
			}
		}
		return result;
	}

	public static void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.printf("%-10.2f", matrix[i][j]);
			}
			System.out.println();
        }
	}

    public static int calculateOptimalThreads(int size) {
        if (size >= 2000) {
            return NUM_THREADS;
        }

        int maxTestThreads = 2 * NUM_THREADS;
        int repeat = 5;
        double bestTime = Double.MAX_VALUE;
        int optimalThreads = 1;

        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);
        multiplyParallel(A, B);

        for (int threads = 1; threads <= maxTestThreads; threads++) {
            NUM_THREADS = threads;
            long totalTime = 0;

            for (int i = 0; i < repeat; i++) {
                long startTime = System.currentTimeMillis();
                multiplyParallel(A, B);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }

            double avgTime = (double) totalTime / repeat;

            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = threads;
            }
        }

        return optimalThreads;
    }

    public static void main(String[] args) {
        int size = 1000;    //размер матриц
        int repeat = 10;

        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);
        // printMatrix(A);
        // printMatrix(B);

        System.out.println("Testing parallel multiply:");

        int threadsToUse = calculateOptimalThreads(size);
        NUM_THREADS = threadsToUse;
        System.out.println("Using threads: " + threadsToUse);

        long totalTime = 0;

        for (int i = 0; i < repeat; i++) {
            long startTime = System.currentTimeMillis();
            double[][] C = multiplyParallel(A, B);
            // printMatrix(C);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
        }

        double averageTime = (double) totalTime / repeat;
        System.out.println("Average time (" + size + " x " + size + "): " + averageTime + " ms\n");
    }
}
