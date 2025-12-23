import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMult {
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int a = firstMatrix.length;          // строки 1
        int b = secondMatrix.length;         // строки 2
        int a1 = firstMatrix[0].length;      // столбцы 1
        int b1 = secondMatrix[0].length;     // столбцы 2
        if (a1 != b) {
            throw new IllegalArgumentException(
                "количество столбцов первой матрицы должно быть равно количеству строк второй матрицы!"
            );
        }
        double[][] result = new double[a][b1];
        int numThreads = Runtime.getRuntime().availableProcessors();
        //int numThreads = 2;
//        System.out.println("доступно ядер: " + numThreads);
//        System.out.println("запуск " + numThreads + " потоков для вычислений.");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int rowsPerThread = a / numThreads;
        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? a : (t + 1) * rowsPerThread;
            executor.submit(() -> {
                String name = Thread.currentThread().getName();
                //System.out.println(name + ": обрабатывает строки [" + startRow + ", " + endRow + ")");
                for (int i = startRow; i < endRow; i++) {
                    for (int k = 0; k < a1; k++) {
                        double r = firstMatrix[i][k];
                        for (int j = 0; j < b1; j++) {
                            result[i][j] += r * secondMatrix[k][j];
                        }
                    }
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return result;
    }

    public static double[][] generatematrix(int row, int col) {
		Random random = new Random();
		double[][] matrix = new double[row][col];
		for(int i = 0; i < row; i++) {
			for(int j = 0; j < col; j++) {
				matrix[i][j] = random.nextDouble(50);
			}
		}
		return matrix;
	}
    
	public static void printmatrix(double[][] matrixforprint){
		for(int i = 0; i < matrixforprint.length; i++) {
			for(int j = 0; j < matrixforprint[i].length; j++) {
				System.out.printf("%10.3f", matrixforprint[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int a = firstMatrix.length; // строки 1
		int b = secondMatrix.length; // строки 2
		int a1 = firstMatrix[0].length; // столбцы 1
		int b1 = secondMatrix[0].length; // столбцы 2
		if(a1 != b) {
			throw new IllegalArgumentException(
					"количество столбцов первой матрицы должно быть равно количеству строк второй матрицы! "
					);
		}
		double [][] result = new double[a][b1];	
		for (int i = 0; i < a; i++) {
	        for (int k = 0; k < a1; k++) { 
	            double r = firstMatrix[i][k]; 
	            for (int j = 0; j < b1; j++) { 
	                result[i][j] += r * secondMatrix[k][j]; 
	            }
	        }
	    }
		
		return result;
	}

    public static void main(String[] args) {
        int size = 1000;
        double[][] matrix1 = null;
        double[][] matrix2 = null;
        double[][] result = null;
        int count = 10;
        long totaltime = 0;
        long totaltimelab1 = 0;
        for (int i = 0; i < count; i++) {
            matrix1 = generatematrix(size, size);
            matrix2 = generatematrix(size, size);
            long startTime = System.currentTimeMillis();
            result = multiplyParallel(matrix1, matrix2); 
            long endTime = System.currentTimeMillis();
            totaltime += (endTime - startTime);
            long startTimelab1 = System.currentTimeMillis();
            result = multiply(matrix1, matrix2); 
            long endTimelab1 = System.currentTimeMillis();
            totaltimelab1 += (endTimelab1 - startTimelab1);
        }

        System.out.println("результаты лаб 7");
        System.out.println("общее время: " + totaltime + " ms");
        System.out.println("среднее время: " + (totaltime / count) + " ms");
        
        System.out.println("результаты лаб 5");
        System.out.println("общее время: " + totaltimelab1 + " ms");
        System.out.println("среднее время: " + (totaltimelab1 / count) + " ms");
    }
}
