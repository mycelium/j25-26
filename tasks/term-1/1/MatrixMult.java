<<<<<<< HEAD

import java.util.Scanner;
import java.util.stream.IntStream;

public class MatrixMult {
	
	static void printMatrix(double[][] Matrix, int row, int col) {
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				System.out.print(Matrix[i][j] + " ");
			
			}

			System.out.println();
		}
	}
	
	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int rowFirst = firstMatrix.length;
		int rowSecond = secondMatrix.length;
		int colFirst = firstMatrix[0].length;
		int colSecond = secondMatrix[0].length;
		
		if(colFirst != rowSecond) {
			System.out.println("\nMultiplication Not Possible");
	            return null;
		}
		
		double[][] result = new double[rowFirst][colSecond];

	    for (int i = 0; i < rowFirst; i++) {
	        for (int j = 0; j < colSecond; j++) {
	            for (int k = 0; k < colFirst; k++) {
	                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
	            }
	        }
	    }

	    return result;
	}
	
	
	public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
	    int rowFirst = firstMatrix.length;
	    int colFirst = firstMatrix[0].length;
	    int colSecond = secondMatrix[0].length;
	    
	    if (colFirst != secondMatrix.length) {
	        System.out.println("\nMultiplication Not Possible");
	        return null;
	    }
	    
	    double[][] result = new double[rowFirst][colSecond];

	    for (int i = 0; i < rowFirst; i++) {
	        double[] resultRow = result[i];
	        double[] firstRow = firstMatrix[i];
	        
	        for (int k = 0; k < colFirst; k++) {
	            double firstValue = firstRow[k];
	            double[] secondRow = secondMatrix[k];
	            
	            for (int j = 0; j < colSecond; j++) {
	                resultRow[j] += firstValue * secondRow[j];
	            }
	        }
	    }
	    
	    return result;
	}


	public static void main(String[] args) {
		   Scanner scan = new Scanner(System.in);
		   
		System.out.println("Matrix Multiplication");
		System.out.println("Enter the sizes for the first matrix");
		 int row1 = scan.nextInt();
		 int col1 = scan.nextInt();
		System.out.println("Enter the sizes for the second matrix");
		 int row2 = scan.nextInt();
		 int col2 = scan.nextInt();
		 
	        double[][] firstMatrix = new double[row1][col1];
	        double[][] secondMatrix = new double[row2][col2];

	        for (int i = 0; i < row1; i++)
	            for (int j = 0; j < col1; j++)
	                firstMatrix[i][j] = Math.random() * 10;

	        for (int i = 0; i < row2; i++)
	            for (int j = 0; j < col2; j++)
	                secondMatrix[i][j] = Math.random() * 10;
	        
	        long startTime = System.nanoTime();
	        double[][] result = multiplyOptimized(firstMatrix, secondMatrix);
	        long endTime = System.nanoTime();
	        
	        long durationNs = endTime - startTime; 
	        double durationSeconds = durationNs / 1_000_000.0;
	        System.out.println("Execution time : " + durationSeconds + " ms");
	        
	       
	        long startTime1 = System.nanoTime();
	        double[][] resultOptimised = multiply(firstMatrix, secondMatrix);
	        long endTime1 = System.nanoTime();
	        
	        long duration = endTime1 - startTime1; 
	        double durationSecon = duration / 1_000_000.0;
	        System.out.println("Execution time optimised : " + durationSecon + " ms");
	}

=======
public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		return new double[1][1];
	}
>>>>>>> 063d39b (First term tasks)
}
