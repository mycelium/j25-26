import java.util.*;

public class MatrixMultPar
{

    private static boolean isValidMatrix(double[][] matrix)
    {
        if (matrix == null || matrix.length == 0)
        {
            return false; // the matrix is empty
        }

        int expectedColumns = matrix[0].length;

        if (expectedColumns == 0)
        {
            return false; // the number of columns is zero
        }

        for (int i = 1; i < matrix.length; i++)
        {
            if (matrix[i].length != expectedColumns)
            {
                return false; // the rows of the matrix are of different lengths
            }
        }
        return true;
    }

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix)
    {
        if (!isValidMatrix(firstMatrix) || !isValidMatrix(secondMatrix))
        {
            throw new IllegalArgumentException("Invalid matrix format for multiplication");
        }

        int colsFirstMatrix = firstMatrix[0].length;
        int rowsSecondMatrix = secondMatrix.length;

        if (colsFirstMatrix != rowsSecondMatrix)
        {
            throw new IllegalArgumentException(
                    "The number of columns in the first matrix is not equal to the number of rows in the second matrix:  " +
                            colsFirstMatrix + " != " + rowsSecondMatrix);
        }

        int rowsFirstMatrix = firstMatrix.length;
        int colsSecondMatrix = secondMatrix[0].length;

        double[][] multiplyResult = new double[rowsFirstMatrix][colsSecondMatrix];

        for (int i = 0; i < rowsFirstMatrix; i++)
        {
            for (int j = 0; j < colsSecondMatrix; j++)
            {
                double sum = 0.0;
                for (int k = 0; k < colsFirstMatrix; k++)
                {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                multiplyResult[i][j] = sum;
            }
        }

        return multiplyResult;
    }

    static class MatrixMultiplierTask implements Runnable
    {
        private final double[][] firstMatrix;
        private final double[][] secondMatrix;
        private final double[][] multiplyResult;
        private final int startRow;
        private final int endRow;

        public MatrixMultiplierTask(double[][] firstMatrix, double[][] secondMatrix,
                                    double[][] multiplyResult, int startRow, int endRow)
        {
            this.firstMatrix = firstMatrix;
            this.secondMatrix = secondMatrix;
            this.multiplyResult = multiplyResult;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run()
        {
            int colsFirstMatrix = firstMatrix[0].length;
            int colsSecondMatrix = secondMatrix[0].length;

            for (int i = startRow; i < endRow; i++)
            {
                for (int j = 0; j < colsSecondMatrix; j++)
                {
                    double sum = 0.0;
                    for (int k = 0; k < colsFirstMatrix; k++)
                    {
                        sum += firstMatrix[i][k] * secondMatrix[k][j];
                    }
                    multiplyResult[i][j] = sum;
                }
            }
        }
    }

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix)
    {

        if (!isValidMatrix(firstMatrix) || !isValidMatrix(secondMatrix))
        {
            throw new IllegalArgumentException("Invalid matrix format for multiplication");
        }

        int colsFirstMatrix = firstMatrix[0].length;
        int rowsSecondMatrix = secondMatrix.length;

        if (colsFirstMatrix != rowsSecondMatrix)
        {
            throw new IllegalArgumentException(
                    "The number of columns in the first matrix is not equal to the number of rows in the second matrix:  " +
                            colsFirstMatrix + " != " + rowsSecondMatrix);
        }

        int rowsFirstMatrix = firstMatrix.length;
        int colsSecondMatrix = secondMatrix[0].length;

        double[][] multiplyResult = new double[rowsFirstMatrix][colsSecondMatrix];

        int threadCount = 16;
        Thread[] threads = new Thread[threadCount];

        // Calculating the number of matrix rows that each thread will process
        int blockSize = (rowsFirstMatrix + threadCount - 1) / threadCount;

        for (int i = 0; i < threadCount; i++)
        {
            int startRow = i * blockSize;
            int endRow = Math.min(startRow + blockSize, rowsFirstMatrix);

            // Use the optimal number of threads
            if (startRow >= rowsFirstMatrix)
            {
                threadCount = i; // Updating the number of actually used threads
                break;
            }

            // Creating a task for the thread
            MatrixMultiplierTask task = new MatrixMultiplierTask(firstMatrix, secondMatrix,
                    multiplyResult, startRow, endRow);

            // Creating and launching a stream
            threads[i] = new Thread(task);
            threads[i].start();
        }

        // Waiting for all threads to complete
        for (int i = 0; i < threadCount; i++)
        {
            if (threads[i] != null)
            {
                try
                {
                    threads[i].join();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted", e);
                }
            }
        }

        return multiplyResult;
    }

    public static boolean areMatricesEqual(double[][] matrix1, double[][] matrix2, double tolerance)
    {
        if (matrix1 == null && matrix2 == null)
        {
            return true;
        }
        if (matrix1 == null || matrix2 == null)
        {
            return false;
        }

        // Checking if the number of rows is the same
        if (matrix1.length != matrix2.length)
        {
            return false;
        }

        // Check each row
        for (int i = 0; i < matrix1.length; i++)
        {
            // Checking if the number of columns in the current row is the same
            if (matrix1[i].length != matrix2[i].length)
            {
                return false;
            }

            // Comparing the elements of the current row
            for (int j = 0; j < matrix1[i].length; j++)
            {
                // We use tolerance to account for rounding errors
                if (Math.abs(matrix1[i][j] - matrix2[i][j]) > tolerance)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean areMatricesEqual(double[][] matrix1, double[][] matrix2)
    {
        return areMatricesEqual(matrix1, matrix2, 1e-9);
    }

    // Method for generating a random matrix
    private static double[][] generateRandomMatrix(int rows, int cols)
    {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                matrix[i][j] = 1.0 + (50.0 - 1.0) * random.nextDouble();
            }
        }
        return matrix;
    }

    // Method for displaying results sorted by execution time
    private static void printResults(Map<String, Double> results)
    {
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("AVERAGE EXECUTION TIME RESULTS");
        System.out.println("=" .repeat(60));

        // Sort algorithms by execution time (from fastest to slowest)
        results.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    System.out.printf("%-15s: %.2f ms%n", entry.getKey(), entry.getValue());
                });

        // Finding the fastest algorithm
        String fastest = results.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .get()
                .getKey();

        System.out.println("\nFastest algorithm: " + fastest);
    }

    public static void main(String[] args)
    {

        try
        {
            int matrixSize = 1000;
            System.out.println("=" .repeat(60));
            System.out.println("Matrix multiplication " + matrixSize + "x" + matrixSize);
            double[][] A = generateRandomMatrix(matrixSize, matrixSize);
            double[][] B = generateRandomMatrix(matrixSize, matrixSize);
            System.out.println("=" .repeat(60));


            // Counting the time for parallel multiplication
            System.out.println("The beginning of parallel multiplication ...");
            long startTime = System.currentTimeMillis();
            double[][] parallelResult = multiplyParallel(A, B);
            long parallelTime = System.currentTimeMillis() - startTime;
            System.out.println("The result for parallel is received");
            System.out.println("=" .repeat(60));

            // Counting the time for single-threaded multiplication
            System.out.println("The beginning of single-threaded multiplication ...");
            startTime = System.currentTimeMillis();
            double[][] singleThreadResult = multiply(A, B);
            long singleThreadTime = System.currentTimeMillis() - startTime;
            System.out.println("The result for single-threaded is received");

            // Проверка корректности
            boolean resultsEqual = areMatricesEqual(parallelResult, singleThreadResult);

            System.out.println("\n" + "=" .repeat(60));
            System.out.println("MULTIPLY RESULTS");
            System.out.println("=" .repeat(60));
            System.out.println("The results match: " + resultsEqual);
            System.out.println("Single-threaded execution: " + singleThreadTime + " мс");
            System.out.println("Parallel execution: " + parallelTime + " мс");
            System.out.printf("Acceleration : %.2f times\n", (double) singleThreadTime / parallelTime);

        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Error in the input data: " + e.getMessage());
        }
        catch (RuntimeException e)
        {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }
}
