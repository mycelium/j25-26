package org.example;
import java.util.Arrays;

public class MatrixMult{

    static double[][] firstMatrix = null;
    static double[][] secondMatrix = {
        {7, 8,7,9,7, 8,7,9,7, 8,7,9,7, 8,7,},
            {7, 8,7,9,7, 8,7,9,7, 8,7,9,7, 2,4,},
            {7, 8,7,9,7, 8,7,9,7, 8,7,9,7, 8,7,},
            {7, 8,7,9,7, 8,7,9,7, 8,7,9,7, 8,7,},

    };

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){

        if(firstMatrix == null || secondMatrix == null){
            System.out.println("Error: One or both matrices are null");
            return null;
        }

        if(firstMatrix.length == 0 || secondMatrix.length == 0){
            System.out.println("Error: One or both matrices are empty");
            return null;
        }

        if(firstMatrix[0] == null || secondMatrix[0] == null){
            System.out.println("Error: Matrix contains null rows");
            return null;
        }

        if(firstMatrix[0].length == 0 || secondMatrix[0].length == 0){
            System.out.println("Error: Matrix contains empty rows");
            return null;
        }

        int firstMatrixCols = firstMatrix[0].length;
        for(int i = 1; i < firstMatrix.length; i++){
            if(firstMatrix[i] == null){
                System.out.println("Error: First matrix has null row at index " + i);
                return null;
            }
            if(firstMatrix[i].length != firstMatrixCols){
                System.out.println("Error: First matrix is irregular. Row 0 has " + firstMatrixCols + " columns, but row " + i + " has " + firstMatrix[i].length + " columns");
                return null;
            }
        }

        int secondMatrixCols = secondMatrix[0].length;
        for(int i = 1; i < secondMatrix.length; i++){
            if(secondMatrix[i] == null){
                System.out.println("Error: Second matrix has null row at index " + i);
                return null;
            }
            if(secondMatrix[i].length != secondMatrixCols){
                System.out.println("Error: Second matrix is irregular. Row 0 has " + secondMatrixCols + " columns, but row " + i + " has " + secondMatrix[i].length + " columns");
                return null;
            }
        }

        if(firstMatrix[0].length!=secondMatrix.length){
            System.out.println("Dimensions of the two matrices are wrong");
            return null;
        }

        long startTime = System.currentTimeMillis();

        double[][] result = new double[firstMatrix.length][secondMatrix[0].length];
        
        int rows = firstMatrix.length;
        int cols = secondMatrix[0].length;
        int shared = firstMatrix[0].length;

        for (int k = 0; k < shared; k++) {
            for (int i = 0; i < rows; i++) {
                double a_ik = firstMatrix[i][k];  // Кэширование этого значения для предотвращения повторного поиска
                for (int j = 0; j < cols; j++) {
                    result[i][j] += a_ik * secondMatrix[k][j];
                }
            }
        }
        /*   Brute force approach

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < shared; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        */


        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        System.out.println(Arrays.deepToString(result));
        System.out.println("ExecutionTime: " + executionTime + " ms");

        return result;
    }

    public static void main(String[] args){
        multiply(firstMatrix,secondMatrix);
    }

}