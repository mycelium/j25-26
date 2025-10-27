public class MatrixMult {
	
	// Стандартное умножение матриц
	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int M = firstMatrix.length;         // Число строк в A
        int K = firstMatrix[0].length;      // Число столбцов в A, должно быть равно числу строк в B
        int N = secondMatrix[0].length;     // Число столбцов в B
        
        // Проверка совместимости размеров
        if (K != secondMatrix.length) {
            throw new IllegalArgumentException(
                "Матрицы несовместимы для умножения. " +
                "Число столбцов первой матрицы (" + K + 
                ") должно быть равно числу строк второй матрицы (" + secondMatrix.length + ")."
            );
        }

        double[][] resultMatrix = new double[M][N];
        
        
        // Цикл по строкам результирующей матрицы C (индекс i)
        for (int i = 0; i < M; i++) {
            
            // Цикл по столбцам результирующей матрицы C (индекс j)
            for (int j = 0; j < N; j++) {
                
                // Цикл по совпадающему размеру K
                double sum = 0;
                for (int k = 0; k < K; k++) {
                    sum += firstMatrix[i][k] * secondMatrix[k][j];
                }
                resultMatrix[i][j] = sum;
            }
        }
        
        return resultMatrix;
    }
	
	// Оптимизированное умножение (по блокам матрци)
	public static double[][] blockMultiply(double[][] firstMatrix, double[][] secondMatrix) {
	    
	    int M = firstMatrix.length;         // Число строк в A
	    int K = firstMatrix[0].length;      // Число столбцов в A / строк в B
	    int N = secondMatrix[0].length;     // Число столбцов в B
	    
	    // Проверка совместимости
	    if (K != secondMatrix.length) {
	        throw new IllegalArgumentException(
	            "Матрицы несовместимы для умножения."
	        );
	    }
	    
	    double[][] resultMatrix = new double[M][N];
	    
	    // Размер блока для разбиения матриц
	    final int BLOCK_SIZE = 64; 

	    // Итерация по блокам столбцов A / строк B
	    for (int kk = 0; kk < K; kk += BLOCK_SIZE) { 
	        
	        // Итерация по блокам строк A / строк C
	        for (int ii = 0; ii < M; ii += BLOCK_SIZE) {
	            
	            // Итерация по блокам столбцов B / столбцов C
	            for (int jj = 0; jj < N; jj += BLOCK_SIZE) {

	                // Стандартное умножение внутри текущих блоков
	                for (int i = ii; i < Math.min(ii + BLOCK_SIZE, M); i++) {
	                    for (int j = jj; j < Math.min(jj + BLOCK_SIZE, N); j++) {
	                        double sum = 0;
	                        
	                        // Цикл по совпадающему размеру (элементам) внутри блока (индекс k)
	                        for (int k = kk; k < Math.min(kk + BLOCK_SIZE, K); k++) {
	                            sum += firstMatrix[i][k] * secondMatrix[k][j];
	                        }
	                        resultMatrix[i][j] += sum; 
	                    }
	                }
	            }
	        }
	    }
	    
	    return resultMatrix;
	}
    
    // Генерация случайной матрицы с заданными размерами
    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 200.0 - 100; 
            }
        }
        return matrix;
    }
    
    public static void main(String[] args) {
    	
    	int[] sizes = {100, 500, 1000, 1500};
    	
        long totalDuration = 0;
        long totalDurationBlock = 0;
        int tries = 10;
        for (int size: sizes) {
        	totalDuration = 0;
        	totalDurationBlock = 0;
	        for (int i = 0; i < tries; ++i) {
		        double[][] A = generateRandomMatrix(size, size);
		        double[][] B = generateRandomMatrix(size, size);
	
		        long startTime = System.currentTimeMillis();
		        double[][] C = multiply(A, B); // Умножение матриц стандартным алгоритмом
		        long endTime = System.currentTimeMillis();
		        long duration = endTime - startTime;
		        totalDuration += duration;
		        startTime = System.currentTimeMillis();
		        C = blockMultiply(A, B); // Умножение матриц оптимизированным блочным методом
		        endTime = System.currentTimeMillis();
		        duration = endTime - startTime;
		        totalDurationBlock += duration;
	        }
	        System.out.println("Размер матриц: " + size + " * " + size);
	        System.out.println("Простое умножение. Среднее время за " + tries + " проверок = " + totalDuration/tries);
	        System.out.println("Блочный алгоритм. Среднее время за " + tries + " проверок = " + totalDurationBlock/tries);
        }
        

        
    }
}