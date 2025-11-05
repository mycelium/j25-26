import java.util.*;

public class Main
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

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix)
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

        // Optimized order: i -> k -> j
        for (int i = 0; i < rowsFirstMatrix; i++)
        {
            for (int k = 0; k < colsFirstMatrix; k++)
            {
                double firstVal = firstMatrix[i][k]; // Caching the value
                for (int j = 0; j < colsSecondMatrix; j++)
                {
                    multiplyResult[i][j] += firstVal * secondMatrix[k][j];
                }
            }
        }

        return multiplyResult;
    }

    public static double[][] multiplyBlocked(double[][] firstMatrix, double[][] secondMatrix, int blockSize)
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

        for (int i0 = 0; i0 < rowsFirstMatrix; i0 += blockSize)
        {
            for (int j0 = 0; j0 < colsSecondMatrix; j0 += blockSize)
            {
                for (int k0 = 0; k0 < colsFirstMatrix; k0 += blockSize)
                {
                    // Processing the block
                    for (int i = i0; i < Math.min(i0 + blockSize, rowsFirstMatrix); i++)
                    {
                        for (int k = k0; k < Math.min(k0 + blockSize, colsFirstMatrix); k++)
                        {
                            double firstVal = firstMatrix[i][k];
                            for (int j = j0; j < Math.min(j0 + blockSize, colsSecondMatrix); j++)
                            {
                                multiplyResult[i][j] += firstVal * secondMatrix[k][j];
                            }
                        }
                    }
                }
            }
        }

        return multiplyResult;
    }

    private static double[][] transpose(double[][] matrix)
    {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposedMatrix = new double[cols][rows];

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }
        return transposedMatrix;
    }

    public static double[][] multiplyWithTranspose(double[][] firstMatrix, double[][] secondMatrix)
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

        // Transpose the second matrix for better data localization
        double[][] secondTransposed = transpose(secondMatrix);

        double[][] multiplyResult = new double[rowsFirstMatrix][colsSecondMatrix];

        for (int i = 0; i < rowsFirstMatrix; i++)
        {
            for (int j = 0; j < colsSecondMatrix; j++)
            {
                double sum = 0.0;
                for (int k = 0; k < colsFirstMatrix; k++)
                {
                    sum += firstMatrix[i][k] * secondTransposed[j][k]; // sequential access (also for 2nd matrix)
                }
                multiplyResult[i][j] = sum;
            }
        }

        return multiplyResult;
    }

    public static double[][] multiplyUnrolled(double[][] firstMatrix, double[][] secondMatrix)
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

        // Deploying the inner loop
        int unrollFactor = 4;
        int remaining = colsSecondMatrix % unrollFactor;

        for (int i = 0; i < rowsFirstMatrix; i++)
        {
            for (int k = 0; k < colsFirstMatrix; k++)
            {
                double firstVal = firstMatrix[i][k];
                int j = 0;

                // The main expanded cycle
                for (; j < colsSecondMatrix - remaining; j += unrollFactor)
                {
                    multiplyResult[i][j] += firstVal * secondMatrix[k][j];
                    multiplyResult[i][j + 1] += firstVal * secondMatrix[k][j + 1];
                    multiplyResult[i][j + 2] += firstVal * secondMatrix[k][j + 2];
                    multiplyResult[i][j + 3] += firstVal * secondMatrix[k][j + 3];
                }

                // Processing the remaining elements
                for (; j < colsSecondMatrix; j++)
                {
                    multiplyResult[i][j] += firstVal * secondMatrix[k][j];
                }
            }
        }

        return multiplyResult;
    }

    public static double[][] multiplyCombined(double[][] firstMatrix, double[][] secondMatrix, int blockSize)
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

        // Using transposition + block algorithm

        double[][] secondTransposed = transpose(secondMatrix);

        double[][] multiplyResult = new double[rowsFirstMatrix][colsSecondMatrix];

        for (int i0 = 0; i0 < rowsFirstMatrix; i0 += blockSize)
        {
            for (int j0 = 0; j0 < colsSecondMatrix; j0 += blockSize)
            {
                for (int k0 = 0; k0 < colsFirstMatrix; k0 += blockSize)
                {
                    // Processing the block
                    for (int i = i0; i < Math.min(i0 + blockSize, rowsFirstMatrix); i++)
                    {
                        for (int j = j0; j < Math.min(j0 + blockSize, colsSecondMatrix); j++)
                        {
                            double sum = multiplyResult[i][j];
                            for (int k = k0; k < Math.min(k0 + blockSize, colsFirstMatrix); k++)
                            {
                                sum += firstMatrix[i][k] * secondTransposed[j][k];
                            }
                            multiplyResult[i][j] = sum;
                        }
                    }
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

    public static void printMatrix(double[][] matrix, String message)
    {
        System.out.println(message);
        System.out.println();

        for (double[] row : matrix)
        {
            for (double val : row)
            {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
    }

    @FunctionalInterface
    interface MatrixMultiplication
    {
        double[][] multiply(double[][] matrix1, double[][] matrix2);
    }

    @FunctionalInterface
    interface MatrixMultiplicationWithBlock
    {
        double[][] multiply(double[][] matrix1, double[][] matrix2, int blockSize);
    }

    // Method for measuring the average execution time
    private static Map<String, Double> measureAverageTime(int matrixSize,
                                                          int iterations,
                                                          int blockSize)
    {
        // Map to store the total time for each algorithm
        Map<String, Long> totalTime = new HashMap<>();

        // List of algorithms for testing
        List<AlgorithmTest> algorithms = Arrays.asList(
                new AlgorithmTest("Classic", (a, b) -> multiply(a, b)),
                new AlgorithmTest("Optimized", (a, b) -> multiplyOptimized(a, b)),
                new AlgorithmTest("With transpose", (a, b) -> multiplyWithTranspose(a, b)),
                new AlgorithmTest("Unrolled", (a, b) -> multiplyUnrolled(a, b)),
                new AlgorithmTest("Blocked", (a, b) -> multiplyBlocked(a, b, blockSize)),
                new AlgorithmTest("Combined", (a, b) -> multiplyCombined(a, b, blockSize))
        );

        // Initializing the total time for each algorithm
        for (AlgorithmTest alg : algorithms)
        {
            totalTime.put(alg.name, 0L);
        }

        // Checking correctness at the last iteration
        double[][] referenceResult = null;
        boolean correctnessChecked = false;

        System.out.println("Starting performance measurement for " + iterations +
                " iterations with matrix size " + matrixSize + "x" + matrixSize);
        System.out.println("Block size: " + blockSize);
        System.out.println("=" .repeat(60));

        for (int iter = 0; iter < iterations; iter++)
        {
            // Generating new random matrices for each iteration
            double[][] matrix1 = generateRandomMatrix(matrixSize, matrixSize);
            double[][] matrix2 = generateRandomMatrix(matrixSize, matrixSize);

            for (AlgorithmTest alg : algorithms)
            {
                long startTime = System.currentTimeMillis();
                double[][] result = alg.algorithm.multiply(matrix1, matrix2);
                long endTime = System.currentTimeMillis();

                long duration = endTime - startTime;
                totalTime.put(alg.name, totalTime.get(alg.name) + duration);

                // Compare the result of the algorithms with the classical calculation
                if (iter == iterations - 1 && !correctnessChecked)
                {
                    if (alg.name.equals("Classic"))
                    {
                        referenceResult = result;
                    }
                    else
                    {
                        boolean isEqual = areMatricesEqual(referenceResult, result);
                        System.out.println("Correctness check for " + alg.name + ": " +
                                (isEqual ? "PASSED" : "FAILED"));
                    }
                }
            }

            // Displaying progress every 10 iterations
            if ((iter + 1) % 10 == 0)
            {
                System.out.println("Completed " + (iter + 1) + "/" + iterations + " iterations");
            }
        }

        // Calculating the average time for each algorithm
        Map<String, Double> averageTime = new LinkedHashMap<>();
        for (AlgorithmTest alg : algorithms)
        {
            double avg = totalTime.get(alg.name) / (double) iterations;
            averageTime.put(alg.name, avg);
        }

        return averageTime;
    }

    // Class for storing information about an algorithm
    private static class AlgorithmTest
    {
        String name;
        MatrixMultiplication algorithm;

        AlgorithmTest(String name, MatrixMultiplication algorithm)
        {
            this.name = name;
            this.algorithm = algorithm;
        }
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
        int matrixSize = 1000;
        int iterations = 50;
        int blockSize = 64;

        try
        {
            Map<String, Double> results = measureAverageTime(matrixSize, iterations, blockSize);
            printResults(results);
        }
        catch (Exception e) {
            System.out.println("Error during performance measurement: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

