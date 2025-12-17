package matrix;

public class MatrixMult {

    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Несовместимые размеры матриц");
        }

        double[][] result = new double[rowsA][colsB];
        int threadCount = getOptimalThreadCount(rowsA, colsB);
        
        //оптимизация через транспонирование
        double[][] secondMatrixT = new double[colsB][rowsB];
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                secondMatrixT[j][i] = secondMatrix[i][j];
            }
        }

        //создание массива потоков, расчет индексов, запуск
        Thread[] threads = new Thread[threadCount];
        int chunkSize = (rowsA + threadCount - 1) / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int startRow = i * chunkSize;
            final int endRow = Math.min(startRow + chunkSize, rowsA);

            if (startRow < endRow) {
                threads[i] = new Thread(() -> {
                    for (int row = startRow; row < endRow; row++) {
                        for (int col = 0; col < colsB; col++) {
                            double sum = 0;
                            for (int k = 0; k < colsA; k++) {
                                sum += firstMatrix[row][k] * secondMatrixT[col][k];
                            }
                            result[row][col] = sum;
                        }
                    }
                });
                threads[i].start();
            }
        }

        //завершение каждого потока
        for (int i = 0; i < threadCount; i++) {
            if (threads[i] != null) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Поток был прерван", e);
                }
            }
        }

        return result;
    }

    public static int getOptimalThreadCount(int rows, int cols) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int matrixSize = Math.max(rows, cols);
        if (matrixSize < 100) return 1;
        if (matrixSize < 300) return Math.max(2, availableProcessors / 4);
        if (matrixSize < 800) return Math.max(4, availableProcessors / 2);
        return availableProcessors;
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int colsB = secondMatrix[0].length;
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

    public static void testPerformance() {
        int[] sizes = {100, 200, 500, 1000, 2000};
        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(30));
            System.out.println("Матрица " + size + "x" + size);
            System.out.println("=".repeat(30));
            double[][] matrixA = generateRandomMatrix(size, size);
            double[][] matrixB = generateRandomMatrix(size, size);

            long startTime = System.currentTimeMillis();
            multiplyOptimized(matrixA, matrixB);
            long seqTime = System.currentTimeMillis() - startTime;
            System.out.println("Последовательно: " + seqTime + " ms");

            startTime = System.currentTimeMillis();
            multiplyParallel(matrixA, matrixB);
            long parTime = System.currentTimeMillis() - startTime;
            System.out.println("Параллельно:    " + parTime + " ms");
            
            if (seqTime > 0 && parTime > 0) {
                System.out.printf("Ускорение: %.2fx\n", (double) seqTime / parTime);
            }
        }
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
        System.out.println("Доступно процессоров: " + Runtime.getRuntime().availableProcessors());
        testPerformance();
    }
}
