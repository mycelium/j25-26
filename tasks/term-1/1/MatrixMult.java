public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){

    // Проверка, что матрицы не пустые и не null
		if (firstMatrix == null || secondMatrix == null) {
        throw new IllegalArgumentException("Матрицы не могут быть null");
    }
    
    if (firstMatrix.length == 0 || firstMatrix[0].length == 0) {
        throw new IllegalArgumentException("Первая матрица не может быть пустой");
    }
    
    if (secondMatrix.length == 0 || secondMatrix[0].length == 0) {
        throw new IllegalArgumentException("Вторая матрица не может быть пустой");
    }

    int m = firstMatrix.length;
    int n = firstMatrix[0].length;
    int p = secondMatrix[0].length;

    // Проверка совместимости матриц
    if (n != secondMatrix.length) {
        throw new IllegalArgumentException("Несовместимые размеры матриц");
    }

        
    double[][] result = new double[m][p];
        
    long startTime = System.nanoTime();
        
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < p; j++) {
            for (int k = 0; k < n; k++) {
                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
            }
        }
    }
        
    long endTime = System.nanoTime();
    System.out.println("Время выполнения: " + (endTime - startTime) / 1_000_000.0 + " мс");
        
        return result;
    }


	public static double[][] multiplyOptimizedUnrolled(double[][] firstMatrix, double[][] secondMatrix) {
    
    // Проверка, что матрицы не пустые и не null
	if (firstMatrix == null || secondMatrix == null) {
        throw new IllegalArgumentException("Матрицы не могут быть null");
    }
    
    if (firstMatrix.length == 0 || firstMatrix[0].length == 0) {
        throw new IllegalArgumentException("Первая матрица не может быть пустой");
    }
    
    if (secondMatrix.length == 0 || secondMatrix[0].length == 0) {
        throw new IllegalArgumentException("Вторая матрица не может быть пустой");
    }
    
    int m = firstMatrix.length;
    int n = firstMatrix[0].length;
    int p = secondMatrix[0].length;

    // Проверка совместимости матриц
        if (n != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }


    double[][] result = new double[m][p];

	long startTime = System.nanoTime();
    
    // Определение степень развёртывания
    final int UNROLL_FACTOR = 4;
    
    for (int i = 0; i < m; i++) {
        for (int k = 0; k < n; k++) {
            double temp = firstMatrix[i][k]; 
            double[] resultRow = result[i];   // Локальная ссылка на строку результата
            double[] secondRow = secondMatrix[k]; // Локальная ссылка на строку второй матрицы
            
            int j = 0;
            for (; j <= p - UNROLL_FACTOR; j += UNROLL_FACTOR) {
                //4 операции за одну итерацию
                resultRow[j]     += temp * secondRow[j];
                resultRow[j + 1] += temp * secondRow[j + 1];
                resultRow[j + 2] += temp * secondRow[j + 2];
                resultRow[j + 3] += temp * secondRow[j + 3];
            }
            
            for (; j < p; j++) {
                resultRow[j] += temp * secondRow[j];
            }
        }
    }

	long endTime = System.nanoTime();
    System.out.println("Время выполнения (оптимизированный алгоритм): " + (endTime - startTime) / 1_000_000.0 + " мс");

    return result;

	
}

public static void main(String[] args) {
        System.out.println("=== Тестирование умножения матриц ===");
        
        //Сравнение производительности на больших матрицах
        System.out.println("\nСравнение производительности:");
        int size = 500; // Можно изменить размер для тестирования
        System.out.println("Размер матриц: " + size + "x" + size);
        
        double[][] bigA = generateRandomMatrix(size, size);
        double[][] bigB = generateRandomMatrix(size, size);
        
        System.out.println("\nИзмерение времени работы:");
        multiply(bigA, bigB);
        multiplyOptimizedUnrolled(bigA, bigB);
        
        
    }
    
    //Печать матриц
    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%8.1f", val);
            }
            System.out.println();
        }
        System.out.println();
    }
    
    //Генерация случайных матриц
    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 10; // Числа от 0 до 10
            }
        }
        return matrix;
    }

}
