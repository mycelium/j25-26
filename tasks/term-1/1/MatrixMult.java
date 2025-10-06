public class MatrixMult {
    public static void main(String[] args) {
        int numberOfTests = 10;
        int[] sizes = {100, 500, 1000, 2000};

        for (int size : sizes) {
            System.out.printf("\n\nTests for %dx%d matrix:\n", size, size);

            double[][] firstMatrix = createMatrix(size, size, 1, 100);
            double[][] secondMatrix = createMatrix(size, size, 1, 100);

            long totalTimeSimple = 0;
            long totalTimeEffective = 0;

            for (int testNum = 0; testNum < numberOfTests; testNum++) {
                try {
                    long startTime = System.currentTimeMillis();
                    multiply(firstMatrix, secondMatrix);
                    long endTime = System.currentTimeMillis();
                    long durationSimple = endTime - startTime;
                    totalTimeSimple += durationSimple;

                    startTime = System.currentTimeMillis();
                    multiplyEffective(firstMatrix, secondMatrix);
                    endTime = System.currentTimeMillis();
                    long durationEffective = endTime - startTime;
                    totalTimeEffective += durationEffective;

                    System.out.println("Execution time for iteration " +
                            (testNum + 1) + " for simple method: " + durationSimple + " ms");
                    System.out.println("Execution time for iteration " +
                            (testNum + 1) + " for effective method: " + durationEffective + " ms");
                }
                catch (ArithmeticException e) {
                    System.out.println("\nError occured: "  + e.getMessage());
                }
            }


            System.out.printf("---- TEST RESULTS FOR %dx%d MATRIX ----\n", size, size);
            System.out.println("Total execution time for simple method: "
                    + totalTimeSimple + " ms");
            System.out.println("Total execution time for effective method: "
                    + totalTimeEffective + " ms");
            System.out.println("Average execution time for simple method: "
                    + (totalTimeSimple / numberOfTests) + " ms");
            System.out.println("Average execution time for effective method: "
                    + (totalTimeEffective / numberOfTests) + " ms");

            System.out.println("\n");
        }


    }

    public static double[][]
        multiply(double[][] firstMatrix, double[][] secondMatrix)
            throws ArithmeticException {
        int firstSizeN = firstMatrix.length;
        int firstSizeM = firstMatrix[0].length;
        int secondSizeN = secondMatrix.length;
        int secondSizeM = secondMatrix[0].length;

        if (firstSizeM != secondSizeN) {
            throw new ArithmeticException("Matrix sizes do not match!");
        }
        double[][] output = new double[firstSizeN][secondSizeM];

        for (int i = 0; i < firstSizeN; i++){
            for (int j = 0; j < secondSizeM; j++){
                for (int k = 0; k < firstSizeM; k++){
                    output[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return output;
    }



    public static double[][]
        multiplyEffective(double[][] firstMatrix, double[][] secondMatrix)
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


    public static double[][] createMatrix(int rowSize, int colSize, int minVal, int maxVal){
        double[][] matrix = new double[rowSize][colSize];
        for (int i = 0; i < rowSize; i++){
            for (int j = 0; j < colSize; j++){
                matrix[i][j] = Math.random() * (maxVal - minVal + 1) + minVal;
            }
        }
        return matrix;
    }

    public static void printMatrix(double[][] matrix, String matrixName) {
        System.out.println("Printing matrix " + matrixName + ":");

        int sizeN = matrix.length;
        int sizeM = matrix[0].length;
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%.2f  ", value);
            }
            System.out.println();
        }
    }
}
