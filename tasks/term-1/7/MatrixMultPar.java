import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {
    
   
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
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
    
  
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threads) {
        int a = firstMatrix.length;
        int b = secondMatrix.length;
        int a1 = firstMatrix[0].length;
        int b1 = secondMatrix[0].length;
        
        if (a1 != b) {
            throw new IllegalArgumentException("Incompatible matrix dimensions");
        }
        
        double[][] result = new double[a][b1];
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        
        
        int rowsPerThread = a / threads;
        int extraRows = a % threads;
        
        int currentRow = 0;
        for (int t = 0; t < threads; t++) {
            final int startRow = currentRow;
            final int endRow = startRow + rowsPerThread + (t < extraRows ? 1 : 0);
            currentRow = endRow;
            
            executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    double[] resultRow = result[i];
                    double[] firstRow = firstMatrix[i];
                    
                    for (int k = 0; k < a1; k++) {
                        double firstValue = firstRow[k];
                        double[] secondRow = secondMatrix[k];
                        
                        for (int j = 0; j < b1; j++) {
                            resultRow[j] += firstValue * secondRow[j];
                        }
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        return result;
    }
    
   
    private static double[][] generateRandomMatrix(int rows, int cols) {
        Random rand = new Random();
        double[][] matrix = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble() * 10;
            }
        }
        
        return matrix;
    }
    
   
    private static boolean areMatricesEqual(double[][] a, double[][] b, double epsilon) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    
    public static void main(String[] args) {
        int row1 = 1000, col1 = 1000;
        int row2 = 1000, col2 = 1000;
        
       
        System.out.println("Matrix A: " + row1 + " x " + col1);
        System.out.println("Matrix B: " + row2 + " x " + col2);
        System.out.println();
        
       
        System.out.println("Generating random matrices...");
        double[][] firstMatrix = generateRandomMatrix(row1, col1);
        double[][] secondMatrix = generateRandomMatrix(row2, col2);
        
     
        System.out.println("\nFirst Version ");
        long startTime = System.nanoTime();
        double[][] resultSeq = multiply(firstMatrix, secondMatrix);
        long endTime = System.nanoTime();
        
        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000.0;
        
        System.out.println("Execution time: " + durationMs + " ms");
          
        int threads = 8;         
        System.out.println("\n Parallel Version (" + threads + " threads) ");
        long startTimePar = System.nanoTime();
        double[][] resultPar = multiplyParallel(firstMatrix, secondMatrix, threads);
        long endTimePar = System.nanoTime();
        
        long durationParNs = endTimePar - startTimePar;
        double durationParMs = durationParNs / 1_000_000.0;
        
        System.out.println("Execution time: " + durationParMs + " ms");
           
        double speedup = durationMs / durationParMs;
        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.printf("Efficiency: %.1f%%%n", (speedup / threads) * 100);
        
    
        if (resultSeq != null && resultPar != null) {
            boolean equal = areMatricesEqual(resultSeq, resultPar, 0.0001);
        }
        
        
        System.out.println("\n Testing Different Thread Counts");
        int maxThreads = 16;
        
        System.out.println("Threads | Time (ms) | Speedup");
        System.out.println("--------|-----------|--------");
        
        for (int t = 1; t <= maxThreads; t++) {
           
            System.gc();
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            long start = System.nanoTime();
            double[][] testResult = multiplyParallel(firstMatrix, secondMatrix, t);
            long end = System.nanoTime();
            
            double timeMs = (end - start) / 1_000_000.0;
            double sp = durationMs / timeMs;
            double eff = (sp / t) * 100;
                     
            System.out.printf("%7d | %9.2f | %7.2f%n",
               t,timeMs, sp );
        }
        
      
    }
}