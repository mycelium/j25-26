public class MatrixMultPar {

    private static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static double[][] multiplyParallel(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Невозможно умножить матрицы: число столбцов первой не равно числу строк второй");
        }

        double[][] result = new double[rowsA][colsB];
        int numThreads = Math.min(THREAD_COUNT, rowsA);

        int baseRows = rowsA / numThreads;
        int extraRows = rowsA % numThreads;

        Thread[] workers = new Thread[numThreads];
        int currentRow = 0;

        for (int t = 0; t < numThreads; t++) {
            int rowsThisThread = baseRows + (t < extraRows ? 1 : 0);
            int start = currentRow;
            int end = start + rowsThisThread;

            final int from = start;
            final int to = end;

            workers[t] = new Thread(() -> {
                for (int i = from; i < to; i++) {
                    for (int k = 0; k < colsA; k++) {
                        double aVal = a[i][k];
                        for (int j = 0; j < colsB; j++) {
                            result[i][j] += aVal * b[k][j];
                        }
                    }
                }
            });

            workers[t].start();
            currentRow = end;
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Выполнение потока было прервано", e);
            }
        }

        return result;
    }

    public static double[][] multiplySequential(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int k = 0; k < colsA; k++) {
                double aVal = a[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += aVal * b[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] createMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = 10.0 + Math.random() * 90.0;
            }
        }
        return matrix;
    }

    public static int findOptimalThreadCount(int size) {
        if (size >= 1500) {
            return Runtime.getRuntime().availableProcessors();
        }

        double[][] A = createMatrix(size, size);
        double[][] B = createMatrix(size, size);
        multiplyParallel(A, B);

        int maxThreads = Math.min(2 * Runtime.getRuntime().availableProcessors(), 8);
        int bestThreads = 1;
        long bestTime = Long.MAX_VALUE;

        for (int t = 1; t <= maxThreads; t++) {
            THREAD_COUNT = t;
            long totalTime = 0;
            int trials = 5;

            for (int i = 0; i < trials; i++) {
                long start = System.nanoTime();
                multiplyParallel(A, B);
                long end = System.nanoTime();
                totalTime += (end - start);
            }

            long avgTime = totalTime / trials;
            if (avgTime < bestTime) {
                bestTime = avgTime;
                bestThreads = t;
            }
        }

        return bestThreads;
    }

    public static void main(String[] args) {
        final int SIZE = 1000;
        final int REPEAT = 5;

        System.out.println("Тестирование параллельного умножения матриц:");
        System.out.println("Размер матриц: " + SIZE + " x " + SIZE);

        double[][] A = createMatrix(SIZE, SIZE);
        double[][] B = createMatrix(SIZE, SIZE);

        int optimal = findOptimalThreadCount(SIZE);
        THREAD_COUNT = optimal;
        System.out.println("Используется потоков: " + optimal);

        long totalParallel = 0;
        for (int i = 0; i < REPEAT; i++) {
            long start = System.nanoTime();
            multiplyParallel(A, B);
            long end = System.nanoTime();
            totalParallel += (end - start);
        }
        double avgParallelMs = (totalParallel / (double) REPEAT) / 1_000_000.0;

        long totalSequential = 0;
        for (int i = 0; i < REPEAT; i++) {
            long start = System.nanoTime();
            multiplySequential(A, B);
            long end = System.nanoTime();
            totalSequential += (end - start);
        }
        double avgSequentialMs = (totalSequential / (double) REPEAT) / 1_000_000.0;

        System.out.printf("Время однопоточной версии: %.2f мс%n", avgSequentialMs);
        System.out.printf("Время многопоточной версии: %.2f мс%n", avgParallelMs);
        System.out.printf("Ускорение: %.2fx%n", avgSequentialMs / avgParallelMs);
    }
}