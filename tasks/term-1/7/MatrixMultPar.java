import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixMultPar {
    static int numTreads = 1;
    public static void setNumThreads(int num) { numTreads = num; }


    public static void main(String[] args) {
//        setNumThreads(4);
//        checkCalc();

//        for (int size : new int[] {500, 1000, 2000}){
//            int optimal = findOptimalNumberOfThreads(size, 10, 16);
//            System.out.printf("Optimal number of threads for %dx%d matrix is: %d\n", size, size, optimal);
//        }

        setNumThreads(8);

        int numberOfTests = 10;
        int[] sizes = {100, 500, 1000, 2000};

        for (int size : sizes) {
            System.out.printf("\n\nTests for %dx%d matrix:\n", size, size);

            double[][] firstMatrix = createMatrix(size, size, 1, 100);
            double[][] secondMatrix = createMatrix(size, size, 1, 100);

            long totalTime = 0;

            for (int testNum = 0; testNum < numberOfTests; testNum++) {
                try {
                    long startTime = System.currentTimeMillis();
                    multiplyParallel(firstMatrix, secondMatrix);
                    long  endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    totalTime += duration;

                    System.out.println("Execution time for iteration " +
                            (testNum + 1) + " for parallel multiply: " + duration + " ms");
                }
                catch (ArithmeticException e) {
                    System.out.println("\nError occured: "  + e.getMessage());
                }
            }


            System.out.printf("---- TEST RESULTS FOR %dx%d MATRIX ----\n", size, size);
            System.out.println("Total execution time for effective method: "
                    + totalTime + " ms");
            System.out.println("Average execution time for effective method: "
                    + (totalTime / numberOfTests) + " ms");

            System.out.println("\n");
        }
    }




    public static int findOptimalNumberOfThreads(int size, int numberOfTests, int maxThreads) {
        double[][] firstMatrix = createMatrix(size, size, 1, 100);
        double[][] secondMatrix = createMatrix(size, size, 1, 100);

        long bestTime = Long.MAX_VALUE;
        int optimalNumberOfThreads = 2;

        for (int threads = 2; threads <= maxThreads; threads++) {
            setNumThreads(threads);

            long totalTime = 0;

            for (int testNum = 0; testNum < numberOfTests; testNum++) {
                try {
                    long startTime = System.currentTimeMillis();
                    multiplyParallel(firstMatrix, secondMatrix);
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    totalTime += duration;

//                    System.out.println("Execution time for iteration " +
//                            (testNum + 1) + " for parallel multiply: " + duration + " ms");
                }
                catch (ArithmeticException e) {
                    System.out.println("\nError occured: "  + e.getMessage());
                }
            }


            System.out.printf(
                    "%d threads; total %d; average %d\n",
                    threads, totalTime, (totalTime / numberOfTests)
            );

            if (totalTime < bestTime) {
                bestTime = totalTime;
                optimalNumberOfThreads = threads;
            }
        }

        return optimalNumberOfThreads;
    }


	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) throws ArithmeticException {
        int n = firstMatrix.length;
        int m = firstMatrix[0].length;
        int l = secondMatrix[0].length;

        if (secondMatrix.length != n) {
            throw new ArithmeticException("Matrix sizes do not match!");
        }


        double[][] output = new double[n][l];
        List<Thread> threads = new ArrayList<>();

        int valuesPerThread = Math.max(1, m / numTreads);

        for (int threadId = 0; threadId < numTreads; threadId++) {
            final int currentId = threadId;

            Thread thread = new Thread(() -> {
                for (int i = currentId; i < m; i += numTreads) {
                    for (int k = 0; k < n; k++) {
                        double value = firstMatrix[i][k];
                        for (int j = 0; j < l; j++){
                            output[i][j] = value * secondMatrix[k][j];
                        }
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Поток был прерван до завершения.", e);
            }
        }

        return output;
	}

    public static double[][] createMatrix(int rowSize, int colSize, int minVal, int maxVal) {
        double[][] matrix = new double[rowSize][colSize];
        List<Thread> threads = new ArrayList<>();

        int rowsPerThread = Math.max(1, rowSize / numTreads);

        for (int threadId = 0; threadId < numTreads; threadId++) {
            final int startRow = rowsPerThread * threadId;
            final int endRow = (threadId == numTreads - 1)
                    ? (rowsPerThread * (threadId + 1))
                    : rowSize;

            Thread thread = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < colSize; j++) {
                        matrix[i][j] = Math.random() * (maxVal - minVal + 1) + minVal;
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Поток был прерван до завершения.", e);
            }
        }

        return matrix;
    }


    public static double[][]
    multiply(double[][] firstMatrix, double[][] secondMatrix)
            throws ArithmeticException{
        int firstSizeN = firstMatrix.length;
        int firstSizeM = firstMatrix[0].length;
        int secondSizeN = secondMatrix.length;
        int secondSizeM = secondMatrix[0].length;

        if (firstSizeM != secondSizeN) {
            throw new ArithmeticException("Matrix sizes do not match!");
        }
        double[][] output = new double[firstSizeN][secondSizeM];

        // Меняем порядок обхода для эффективного использования буфферизации
        for (int k = 0; k < firstSizeM; k++){
            for (int i = 0; i < firstSizeN; i++){
                double value = firstMatrix[i][k];
                for (int j = 0; j < secondSizeM; j++){
                    output[i][j] = value * secondMatrix[k][j];
                }
            }
        }

        return output;
    }

    public static void checkCalc() {
        int numberOfTests = 10;
        int[] sizes = {100, 500, 1000};

        for (int size : sizes) {
            System.out.printf("\n\nTests for %dx%d matrix:\n", size, size);

            double[][] firstMatrix = createMatrix(size, size, 1, 100);
            double[][] secondMatrix = createMatrix(size, size, 1, 100);

            long totalTime = 0;
            int successful = 0;

            for (int testNum = 0; testNum < numberOfTests; testNum++) {
                try {
                    double[][] multMatrix = multiply(firstMatrix, secondMatrix);
                    double[][] parallelMatrix = multiplyParallel(firstMatrix, secondMatrix);

                    if (Arrays.deepEquals(multMatrix, parallelMatrix)) {
                        System.out.printf(
                                "\nTest for %d for %dx%d matrix is successful.",
                                testNum+1,
                                size,
                                size
                        );
                        successful++;
                    }
                    else {
                        System.err.println("Вычисленные матрицы отличаются!");
                    }
                }
                catch (ArithmeticException e) {
                    System.out.println("\nError occured: "  + e.getMessage());
                }
            }

            System.out.printf("\n---- TEST RESULTS FOR %dx%d MATRIX ----\n", size, size);
            System.out.println("Success rate: " + (successful / numberOfTests)*100 + "%");

            System.out.println("\n");
        }
    }
}
