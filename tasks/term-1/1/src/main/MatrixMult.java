package main;

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
        Matrix first = new Matrix(firstMatrix);
        Matrix second = new Matrix(secondMatrix);

        return first.multiply(second);
    }

}
