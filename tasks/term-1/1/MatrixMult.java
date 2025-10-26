import java.util.Scanner;
public class matrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        double[][] result = new double[rowsA][colsB];
        if(colsA!=rowsB){
            System.out.println("Matrixes can't be multiplicated, because number of cols in first matrix("+colsA+")!=" +
                    "number of rows in second matrix("+rowsB+")");
            return result;
        }



        for (int k = 0; k < colsA; k++) {
            for (int i = 0; i < rowsA; i++) {
                double val = firstMatrix[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += val * secondMatrix[k][j];
                }
            }
        }

        return result;
    }
    public static double[][] standartMult(double[][] firstMatrix, double[][] secondMatrix){
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        double[][] result = new double[rowsA][colsB];

        if(colsA!=rowsB){
            System.out.println("Matrixes can't be multiplicated, because number of cols in first matrix("+colsA+")!=" +
                    "number of rows in second matrix("+rowsB+")");
            return result;
        }

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return result;
    }
    public static double[][] RandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        Scanner in=new Scanner(System.in);
        System.out.print("Enter matrix size: ");
        int size = in.nextInt();
        System.out.print("Enter number of iterations: ");
        int iterations = in.nextInt();

        double[][] matrixA = RandomMatrix(size, size, 1, 10);
        double[][] matrixB = RandomMatrix(size, size, 1, 10);

        long totalTimeFast = 0;
        long totalTimeLong = 0;

        for (int i = 0; i < iterations; i++){
            long startTimeFast = System.currentTimeMillis();
            multiply(matrixA, matrixB);
            long endTimeFast = System.currentTimeMillis();

            long startTimeLong = System.currentTimeMillis();
            standartMult(matrixA, matrixB);
            long endTimeLong = System.currentTimeMillis();

            long durationFast = endTimeFast-startTimeFast;
            totalTimeFast += durationFast;

            long durationLong = endTimeLong - startTimeLong ;
            totalTimeLong += durationLong;
        }

        System.out.println("=== RESULTS ===");
        System.out.println("Average time for standart mult: " + (totalTimeLong / iterations) + " ms");
        System.out.println("Average time for optimized mult: " + (totalTimeFast / iterations) + " ms");
    }
}
