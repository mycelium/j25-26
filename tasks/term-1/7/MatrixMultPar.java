import java.util.concurrent.*;

public class MatrixMultPar {
    private static int threads = 1;

    public static void setThreads (int n) { threads = n; }

    public static int getThreads() { return threads; }
    
	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;

        if (colsA != secondMatrix.length) {
            throw new IllegalArgumentException("Неподходящие размеры матриц для умножения");
        }

        double[][] result = new double[rowsA][colsB];
        
        int optimalThreads = getThreads();
        ExecutorService executor = Executors.newFixedThreadPool(optimalThreads);
        
        int rowsPerThread = Math.max(1, rowsA / optimalThreads);

        try {
            for (int i = 0; i < optimalThreads; i++) {
                final int startRow = i * rowsPerThread;
                final int endRow = (i == optimalThreads - 1) ? rowsA : (i + 1) * rowsPerThread;

                executor.execute(() -> {
                    for (int row = startRow; row < endRow; row++) {
                        for (int col = 0; col < colsB; col++) {
                            double sum = 0;
                            for (int k = 0; k < colsA; k++) {
                                sum += firstMatrix[row][k] * secondMatrix[k][col];
                            }
                            result[row][col] = sum;
                        }
                    }
                });
            }
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Прервано во время выполнения", e);
            }
        }

        return result;
	}
    
    // из 1 лабы
    public static double[][] multiplySingleThread(double[][] firstMatrix, double[][] secondMatrix){
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        // тестирование на разных размерах матриц
        int[] sizes = {500, 1000, 2000};
        int[] numThreads = {2, 4, 8, 16, 20, 24};
        
        for (int size : sizes) {
            System.out.println("\n=== Матрица " + size + "x" + size + " ===");

            double[][] matrixA = generateRandomMatrix(size, size);
            double[][] matrixB = generateRandomMatrix(size, size);

            // Однопоточное умножение
            long startTime = System.currentTimeMillis();
            double[][] singleThreadResult = multiplySingleThread(matrixA, matrixB);
            long singleThreadTime = System.currentTimeMillis() - startTime;
            System.out.println("Однопоточное время: " + singleThreadTime + " мс");
            
            // Параллельное умножение
            for (int n : numThreads) {
                setThreads(n);
                startTime = System.currentTimeMillis();
                double[][] parallelResult = multiplyParallel(matrixA, matrixB);
                long parallelTime = System.currentTimeMillis() - startTime;
                System.out.println("Параллельное время (" + getThreads() + " потока/ов): " + parallelTime + " мс");
            }
            // Вывод результатов
            
            //System.out.printf("Ускорение: %.2fx\n", (double) singleThreadTime / parallelTime);
            //System.out.println("Количество потоков: " + getThreads());
        }
    }
}
