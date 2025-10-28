package src.main;

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        Matrix first = null;
        try {
            first = new Matrix(firstMatrix);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while creating first matrix: " + e.getMessage());
            System.exit(1);
        }

        Matrix second = null;
        try {
            second = new Matrix(secondMatrix);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while creating second matrix: " + e.getMessage());
            System.exit(1);
        }

        double[][] result = null;
        try {
            result = first.multiply(second);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Error while multiplying matrices: " + e.getMessage());
            System.exit(1);
        }

        return result;
    }

}
