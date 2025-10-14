
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.concurrent.ForkJoinPool;

public class MatrixMultPar {

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

	
	
	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix,  int threads) {
	    int n = firstMatrix.length, p = firstMatrix[0].length, m = secondMatrix[0].length;
	    if (p != secondMatrix.length) throw new IllegalArgumentException("Incompatible dimensions");

	    double[][] result = new double[n][m];
		 ForkJoinPool pool = new ForkJoinPool(threads);
		 try {
            pool.submit(() ->

	    IntStream.range(0, n).parallel().forEach(i -> {
	        double[] Ai = firstMatrix[i];
	        double[] Ci = result[i];
	        for (int k = 0; k < p; k++) {
	            double a_ik = Ai[k];
	            double[] Bk = secondMatrix[k];
	            for (int j = 0; j < m; j++) {
	                Ci[j] += a_ik * Bk[j];
	            }
	        }
	    })
            		 ).join(); 
		    } finally {
		        pool.shutdown();
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
	        
	       int threads = 8;
	        long startTime1 = System.nanoTime();
	        double[][] resultOptimised = multiplyParallel(firstMatrix, secondMatrix, threads);
	        long endTime1 = System.nanoTime();
	        
	        long duration = endTime1 - startTime1; 
	        double durationSecon = duration / 1_000_000.0;
	        System.out.println("Execution time optimised : " + durationSecon + " ms");
	}

}
