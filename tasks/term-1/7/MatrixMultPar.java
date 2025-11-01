import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

    private static final int IDEAL_THREAD_AMOUNT = 12;
    private static final int MAX_THREAD_AMOUNT = 2 * Runtime.getRuntime().availableProcessors();

    private static boolean isInvalidMatrix(double[][] matrix) {
        if (matrix == null) {
            return true;
        }

        int cols = matrix[0].length;
        for (double[] matrixRow: matrix) {
            if (matrixRow.length != cols) {
                return true;
            }
        }

        return false;
    }

    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    private static int getBestThreadAmount(int matrixSize, boolean isLogging) {
        int bestThreadAmount = 1;
        double bestThreadAmountTime = Double.MAX_VALUE;
        for (int threadAmount = 1; threadAmount <= MAX_THREAD_AMOUNT; ++threadAmount) {
            double avgTime = rawMeasureMultiplicationPerformance(matrixSize, 10, threadAmount);
            if (isLogging) {
                System.out.printf("""
                                Multiplication performance for %d parallel threads.
                                Multiplying %dx%d matrices over 10 iterations.
                                
                                In average time to multiply is %f ms.%n
                                """,
                        threadAmount,
                        matrixSize,
                        matrixSize,
                        avgTime
                );
            }

            if (bestThreadAmountTime > avgTime) {
                bestThreadAmount = threadAmount;
                bestThreadAmountTime = avgTime;
            }
        }

        if (isLogging) {
            System.out.printf("""
                            =========================================================
                            Best parallel threads amount for this machine is %d.
                            
                            Average elapsed time: %f ms.
                            =========================================================%n
                            """,
                    bestThreadAmount,
                    bestThreadAmountTime
            );
        }

        return bestThreadAmount;
    }

    private static int getBestThreadAmount(int matrixSize) {
        return getBestThreadAmount(matrixSize, false);
    }

    private static double rawMeasureMultiplicationPerformance(int matrixSize, int iterations, int threadsNumber) {
        if (matrixSize <= 0 || iterations <= 0 || threadsNumber <= 0) {
            throw new IllegalArgumentException("Matrix size and iterations and threads number must be positive.");
        }

        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            double[][] matrix1 = generateRandomMatrix(matrixSize, matrixSize);
            double[][] matrix2 = generateRandomMatrix(matrixSize, matrixSize);

            long startTime = System.nanoTime();
            multiplyParallel(matrix1, matrix2, threadsNumber);
            long endTime = System.nanoTime();

            totalTime += endTime - startTime;
        }

        return totalTime / (iterations * 1_000_000.0);
    }

    public static double measureMultiplicationPerformance(int matrixSize, int iterations, int threadAmount) {
        System.out.printf(
                "Testing performance for %dx%d matrices over %d iterations%n",
                matrixSize,
                matrixSize,
                iterations
        );

        double avgTimeMs = rawMeasureMultiplicationPerformance(
                matrixSize, iterations, threadAmount
        );
        System.out.printf("Average time: %.2f ms%n", avgTimeMs);
        return avgTimeMs;
    }

    public static double measureMultiplicationPerformance(int matrixSize, int iterations) {
        return measureMultiplicationPerformance(matrixSize, iterations, IDEAL_THREAD_AMOUNT);
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadsNumber) {
        try {
            if (threadsNumber <= 0) {
                throw new IllegalArgumentException(
                        "Threads amount can not be negative!"
                );
            }
            if (threadsNumber > MAX_THREAD_AMOUNT) {
                throw new IllegalArgumentException(
                        "Too many threads, machine could be overloaded!"
                );
            }

            if (isInvalidMatrix(firstMatrix)) {
                throw new IllegalArgumentException(
                        "First matrix is not valid. " +
                                "You should pass a valid matrix to multiply them!"
                );
            }

            if (isInvalidMatrix(secondMatrix)) {
                throw new IllegalArgumentException(
                        "Second matrix is not valid. " +
                                "You should pass a valid matrix to multiply them!"
                );
            }

            if (firstMatrix[0].length != secondMatrix.length) {
                throw new IllegalArgumentException(
                        "Column amount of the first matrix " +
                                "is not equal to the row amount of the second matrix!"
                );
            }
        }
        catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return null;
        }

        int firstMatrixRows = firstMatrix.length;
        int firstMatrixCols = firstMatrix[0].length;

        int secondMatrixRows = secondMatrix.length;
        int secondMatrixCols = secondMatrix[0].length;

        double[][] secondMatrixTranspose = new double[secondMatrixCols][secondMatrixRows];
        for (int i = 0; i < secondMatrixRows; i++) {
            for (int j = 0; j < secondMatrixCols; j++) {
                secondMatrixTranspose[j][i] = secondMatrix[i][j];
            }
        }

        double[][] result = new double[firstMatrixRows][secondMatrixCols];

        threadsNumber = Math.min(threadsNumber, firstMatrixRows);

        List<Thread> threads = new ArrayList<>(threadsNumber);
        int rowsPerThread = (firstMatrixRows + threadsNumber - 1) / threadsNumber;
        for (int t = 0; t < threadsNumber; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, firstMatrixRows);

            if (startRow >= firstMatrixRows)
                break;

            Thread thread = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    double[] firstMatrixRow = firstMatrix[i];
                    double[] resultRow = result[i];

                    for (int j = 0; j < secondMatrixCols; j++) {
                        double[] secondMatrixCol = secondMatrixTranspose[j];

                        double sum = 0.0;
                        for (int k = 0; k < firstMatrixCols; k++) {
                            sum += firstMatrixRow[k] * secondMatrixCol[k];
                        }
                        resultRow[j] = sum;
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted during join.\n[ERROR] " + e.getMessage());
            }
        }

        return result;
    }

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
        return multiplyParallel(
                firstMatrix, secondMatrix, IDEAL_THREAD_AMOUNT
        );
	}

    public static void main(String[] args) {
        int bestThreadAmount = getBestThreadAmount(500,true);
        measureMultiplicationPerformance(100, 15, bestThreadAmount);
        measureMultiplicationPerformance(500, 15, bestThreadAmount);
        measureMultiplicationPerformance(1000, 15, bestThreadAmount);
        measureMultiplicationPerformance(1500, 15, bestThreadAmount);
    }
}
