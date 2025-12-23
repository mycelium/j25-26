public class MatrixMult {
    private static int num_Threads = Runtime.getRuntime().availableProcessors();

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        double[][] result = new double[rowsA][colsB];
        int numThreads = Runtime.getRuntime().availableProcessors();
        if (colsA != rowsB) {
            System.out.println("Matrixes can't be multiplicated, because number of cols in first matrix("+colsA+")!=" +
                    "number of rows in second matrix("+rowsB+")");
            return result;
        }

        Thread[] threads = new Thread[numThreads];

        int rowsPerThread = (rowsA + numThreads - 1) / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, rowsA);

            threads[t] = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int k = 0; k < colsA; k++) {
                        double val = firstMatrix[i][k];
                        for (int j = 0; j < colsB; j++) {
                            result[i][j] += val * secondMatrix[k][j];
                        }
                    }
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Выполнение прервано", e);
            }
        }

        return result;
    }
    public static void findOptimalThreadCount(double[][] matrixA, double[][] matrixB, int iterations) {
        int maxThreads = Runtime.getRuntime().availableProcessors();//вычисляем максимальное число потоков для моей платформы
        long bestTime = Long.MAX_VALUE;
        int optimalThreads = 0;

        for (int threads = 1; threads <= maxThreads; threads++) {//для каждого числа потоков применяем функцию умножения
            num_Threads=threads;
            long totalTime = 0;

            for (int it = 0; it < iterations; it++) {//умножаем несколько раз, чтобы усреднить время
                long startTime = System.currentTimeMillis();
                multiplyParallel(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            long avgTime = totalTime / iterations;//вычисляем среднее время

            if (avgTime < bestTime) {//сравниваем с лучшим временем
                bestTime = avgTime;
                optimalThreads = threads;//меняем оптимальное число потоков
            }
        }
        num_Threads=optimalThreads;//присваиваем переменной числа потоков оптимальное значение
        System.out.println("Optimal thread count: " + optimalThreads);
        System.out.println("Best average time: " + bestTime + " ms");
    }
    public static double[][] RandomMatrix(int rows, int cols, double min, double max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
    }
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){

        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        double[][] result = new double[rowsA][colsB];
        if(colsA!=rowsB){
            System.out.println("Matrixes can't be multiplicated, because number of cols in first matrix("+colsA+")!=" +
                    "number of rows in second matrix("+rowsB+")");
            return result;
        }
        for (int k = 0; k < colsA; k++) {
            for (int i = 0; i < rowsA; i++) {
                double val = firstMatrix[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += val * secondMatrix[k][j];
                }
            }
        }

        return result;
    }
    public static void main(String[] args) {

        int size = 1000;
        System.out.println("matrix size: "+ size);
        int iterations = 10;
        System.out.println("number of iterations: "+iterations);


        double[][] matrixA = RandomMatrix(size, size, 1, 10);
        double[][] matrixB = RandomMatrix(size, size, 1, 10);

        findOptimalThreadCount(matrixA, matrixB, iterations);

        long totalTimeFast = 0;
        long totalTimeLong = 0;

        for (int i = 0; i < iterations; i++){
            long startTimeFast = System.currentTimeMillis();
            multiplyParallel(matrixA, matrixB);
            long endTimeFast = System.currentTimeMillis();

            long startTimeLong = System.currentTimeMillis();
            multiply(matrixA, matrixB);
            long endTimeLong = System.currentTimeMillis();

            long durationFast = endTimeFast-startTimeFast;
            totalTimeFast += durationFast;

            long durationLong = endTimeLong - startTimeLong ;
            totalTimeLong += durationLong;
        }

        System.out.println("=== RESULTS ===");
        System.out.println("Average time for optimized mult: " + (totalTimeLong / iterations) + " ms");
        System.out.println("Average time for parallel mult: " + (totalTimeFast / iterations) + " ms");


    }

}