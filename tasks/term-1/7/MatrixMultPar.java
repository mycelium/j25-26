package lab7;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {
    
    // Однопоточное умножение из лабы 1
    public static double[][] multiply(double[][] A, double[][] B) {
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;
        
        double[][] C = new double[n][p];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < m; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }
    
    // Параллельное умножение 
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int n = firstMatrix.length;
        int m = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        double[][] result = new double[n][p];
        
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        int rowsPerThread = n / threadCount;
        int extraRows = n % threadCount;
        
        int startRow = 0;
        for (int t = 0; t < threadCount; t++) {
            final int fromRow = startRow;
            final int rowsForThread = rowsPerThread + (t < extraRows ? 1 : 0);
            final int toRow = fromRow + rowsForThread;
            
            executor.execute(() -> {
                for (int i = fromRow; i < toRow; i++) {
                    for (int j = 0; j < p; j++) {
                        double sum = 0;
                        for (int k = 0; k < m; k++) {
                            sum += firstMatrix[i][k] * secondMatrix[k][j];
                        }
                        result[i][j] = sum;
                    }
                }
            });
            
            startRow = toRow;
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return result;
    }
    
    //тестирование разных потоков
    public static double[][] multiplyParallel(double[][] A, double[][] B, int threadCount) {
        int n = A.length;
        int m = A[0].length;
        int p = B[0].length;
        
        double[][] C = new double[n][p];
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        int rowsPerThread = n / threadCount;
        int extraRows = n % threadCount;
        
        int startRow = 0;
        for (int t = 0; t < threadCount; t++) {
            final int fromRow = startRow;
            final int rowsForThread = rowsPerThread + (t < extraRows ? 1 : 0);
            final int toRow = fromRow + rowsForThread;
            
            executor.execute(() -> {
                for (int i = fromRow; i < toRow; i++) {
                    for (int j = 0; j < p; j++) {
                        double sum = 0;
                        for (int k = 0; k < m; k++) {
                            sum += A[i][k] * B[k][j];
                        }
                        C[i][j] = sum;
                    }
                }
            });
            
            startRow = toRow;
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return C;
    }
    
    // Генерация матрицы
    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }
    
    public static void main(String[] args) {
        System.out.println("Параллельное умножение матриц");
        System.out.println("Доступно ядер: " + Runtime.getRuntime().availableProcessors());
        
        int[] sizes = {500, 1000};
        int repeats = 5;
        
        for (int size : sizes) {
            System.out.println("\n=== Матрица " + size + "x" + size + " ===");
            
            double[][] A = generateMatrix(size, size);
            double[][] B = generateMatrix(size, size);
            
            
            for (int i = 0; i < 2; i++) {
                multiply(A, B);
                multiplyParallel(A, B);
            }
            
            // Однопоточное
            long singleTotal = 0;
            for (int i = 0; i < repeats; i++) {
                long start = System.currentTimeMillis();
                multiply(A, B);
                long end = System.currentTimeMillis();
                singleTotal += (end - start);
            }
            long singleAvg = singleTotal / repeats;
            System.out.println("Однопоточное время: " + singleAvg + " мс");
            
            // Параллельное с разными потоками 
            int[] threadCounts = {2, 4, 8};
            for (int threads : threadCounts) {
                long multiTotal = 0;
                for (int i = 0; i < repeats; i++) {
                    long start = System.currentTimeMillis();
                    multiplyParallel(A, B, threads);
                    long end = System.currentTimeMillis();
                    multiTotal += (end - start);
                }
                long multiAvg = multiTotal / repeats;
                System.out.println("Параллельное время (" + threads + " потока/ов): " + multiAvg + " мс");
                
                // Ускорение 
                if (multiAvg > 0) {
                    double speedup = (double) singleAvg / multiAvg;
                    System.out.printf("Ускорение: %.2fx\n", speedup);
                }
            }
        }
    }
}
