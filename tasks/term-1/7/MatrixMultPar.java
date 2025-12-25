import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultPar {

    private static int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    
    /**
     * Параллельное умножение матриц с оптимизированным порядком циклов
     * @param firstMatrix 
     * @param secondMatrix 
     * @return 
     */
    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (n != secondMatrix.length) {
            throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + m + "x" + n + " и " + 
                secondMatrix.length + "x" + p
            );
        }
        double[][] result = new double[m][p];
        
        int threadCount = Math.min(NUM_THREADS, m);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        int rowsPerThread = m / threadCount;
        int remainingRows = m % threadCount;
        int currentRow = 0;
        
        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
            final int startRow = currentRow;
            int rowsForThisThread = rowsPerThread + (threadIndex < remainingRows ? 1 : 0);
            final int endRow = startRow + rowsForThisThread;
            
            executor.submit(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int k = 0; k < n; k++) {
                        double temp = firstMatrix[i][k]; // Кэшируем значение
                        if (Math.abs(temp) > 1e-12) { // Пропускаем нулевые значения
                            for (int j = 0; j < p; j++) {
                                result[i][j] += temp * secondMatrix[k][j];
                            }
                        }
                    }
                }
            });
            
            currentRow = endRow;
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Умножение прервано", e);
        }
        
        return result;
    }
    
    /**
     * Однопоточное умножение 
     */
    public static double[][] multiplySingleThreaded(double[][] firstMatrix, double[][] secondMatrix) {
        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;
        
        if (n != secondMatrix.length) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }
        
        double[][] result = new double[m][p];
        
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                double temp = firstMatrix[i][k];
                if (Math.abs(temp) > 1e-12) {
                    for (int j = 0; j < p; j++) {
                        result[i][j] += temp * secondMatrix[k][j];
                    }
                }
            }
        }
        
        return result;
    }
  
    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 90 + 10; 
            }
        }
        return matrix;
    }
    
    /**
     * Подбор количества потоков
     */
    public static int calculateOptimalThreads(int targetSize) {
        if (targetSize <= 200) {
            return 1;
        }
        if (targetSize >= 2000) {
            return NUM_THREADS;
        }
        int testSize = Math.min(500, targetSize / 2);
        int maxThreads = Math.min(2 * NUM_THREADS, targetSize);
        int repeats = 3;
        
        double[][] A = generateMatrix(testSize, testSize);
        double[][] B = generateMatrix(testSize, testSize);
        
        double bestTime = Double.MAX_VALUE;
        int optimalThreads = 1;
        
        for (int threads = 1; threads <= maxThreads; threads++) {
            NUM_THREADS = threads;
            
            multiplyParallel(A, B);
            
            long totalTime = 0;
            for (int r = 0; r < repeats; r++) {
                long start = System.nanoTime();
                multiplyParallel(A, B);
                long end = System.nanoTime();
                totalTime += (end - start);
            }
            
            double avgTime = totalTime / 1_000_000.0 / repeats; 
            
            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = threads;
            }
        }
        
        return optimalThreads;
    }
    
    public static boolean verifyMultiplication(double[][] parallelResult, 
                                             double[][] singleResult) {
        if (parallelResult.length != singleResult.length || 
            parallelResult[0].length != singleResult[0].length) {
            return false;
        }
        
        double epsilon = 1e-6;
        for (int i = 0; i < parallelResult.length; i++) {
            for (int j = 0; j < parallelResult[0].length; j++) {
                if (Math.abs(parallelResult[i][j] - singleResult[i][j]) > epsilon) {
                    System.err.printf("Ошибка в [%d][%d]: параллельно=%f, однопоточно=%f%n",
                        i, j, parallelResult[i][j], singleResult[i][j]);
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void testPerformance(int size, int repeats) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Тестирование матриц " + size + "x" + size);
        System.out.println("=".repeat(60));
        
        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);
        
        System.out.println("Подбор оптимального количества потоков...");
        NUM_THREADS = calculateOptimalThreads(size);
        System.out.println("Оптимальное количество потоков: " + NUM_THREADS);
        
        System.out.println("\nОднопоточное умножение:");
        long singleTotal = 0;
        double[][] singleResult = null;
        
        for (int i = 0; i < repeats; i++) {
            long start = System.nanoTime();
            singleResult = multiplySingleThreaded(A, B);
            long end = System.nanoTime();
            singleTotal += (end - start);
        }
        double singleAvg = singleTotal / 1_000_000.0 / repeats;
        System.out.printf("Среднее время: %.2f мс%n", singleAvg);
        
        System.out.println("\nПараллельное умножение:");
        long parallelTotal = 0;
        double[][] parallelResult = null;
        
        multiplyParallel(A, B);
        
        for (int i = 0; i < repeats; i++) {
            long start = System.nanoTime();
            parallelResult = multiplyParallel(A, B);
            long end = System.nanoTime();
            parallelTotal += (end - start);
        }
        double parallelAvg = parallelTotal / 1_000_000.0 / repeats;
        System.out.printf("Среднее время: %.2f мс%n", parallelAvg);
        
        double speedup = singleAvg / parallelAvg;
        System.out.printf("\nУскорение: %.2f раз%n", speedup);
        System.out.printf("Эффективность: %.1f%%%n", (speedup / NUM_THREADS) * 100);
        
        System.out.println("\nПроверка корректности...");
        boolean correct = verifyMultiplication(parallelResult, singleResult);
        System.out.println("Результаты " + (correct ? "совпадают ✓" : "не совпадают ✗"));
    }
    
    public static void main(String[] args) {
        System.out.println("Процессоров доступно: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Памяти доступно: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        
        int[] testSizes = {500, 1000, 1500};
        int repeats = 5;
        
        for (int size : testSizes) {
            try {
                testPerformance(size, repeats);
            } catch (OutOfMemoryError e) {
                System.err.println("Недостаточно памяти для матрицы " + size + "x" + size);
                break;
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Финальный тест с большой матрицей");
        System.out.println("=".repeat(60));
        
        try {
            int bigSize = 2000;
            if (Runtime.getRuntime().freeMemory() > bigSize * bigSize * 8 * 3) {
                testPerformance(bigSize, 3);
            } else {
                System.out.println("Недостаточно памяти для матрицы " + bigSize + "x" + bigSize);
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Тест с большой матрицей пропущен из-за нехватки памяти");
        }
    }
}