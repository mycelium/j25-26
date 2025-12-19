public class MatrixMultPar {

    private static int THREAD_LIMIT = 8;

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
        if (firstMatrix == null || secondMatrix == null ||
                firstMatrix.length == 0 || secondMatrix.length == 0 ||
                firstMatrix[0] == null || secondMatrix[0] == null) {
            System.out.println("Одна из матриц пустая.");
            return null;
        }
        if(firstMatrix[0].length != secondMatrix.length){
            System.out.println("Количество столбцов первой матрицы не равно количеству строк второй матрицы, перемножение невозможно");
            return null;
        }
        double[][] result = new double[firstMatrix.length][secondMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int k = 0; k < firstMatrix[0].length; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < secondMatrix[0].length; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }

        return result;
    }

    private static class RowWorker implements Runnable {

        private final double[][] left;
        private final double[][] right;
        private final double[][] output;
        private final int fromRow;
        private final int toRow;

        public RowWorker(double[][] left,
                         double[][] right,
                         double[][] output,
                         int fromRow,
                         int toRow) {
            this.left = left;
            this.right = right;
            this.output = output;
            this.fromRow = fromRow;
            this.toRow = toRow;
        }

        @Override
        public void run() {
            int commonSize = left[0].length;
            int resultCols = right[0].length;

            for (int i = fromRow; i < toRow; i++) {
                for (int k = 0; k < commonSize; k++) {
                    double factor = left[i][k];
                    for (int j = 0; j < resultCols; j++) {
                        output[i][j] += factor * right[k][j];
                    }
                }
            }
        }
    }

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix){
        if (firstMatrix == null || secondMatrix == null || firstMatrix.length == 0 || secondMatrix.length == 0) {
            System.out.println("Одна из матриц пустая.");
            return null;
        }

        if (firstMatrix[0].length != secondMatrix.length) {
            System.out.println("Количество столбцов первой матрицы не равно количеству строк второй матрицы, перемножение невозможно");
            return null;
        }

        int rows = firstMatrix.length;
        int cols = secondMatrix[0].length;

        double[][] result = new double[rows][cols];

        Thread[] workers = new Thread[THREAD_LIMIT];

        int chunk = Math.max(1, rows / THREAD_LIMIT);

        for (int t = 0; t < THREAD_LIMIT; t++) {
            int start = t * chunk;
            int end = (t == THREAD_LIMIT - 1) ? rows : start + chunk;

            workers[t] = new Thread(
                    new RowWorker(firstMatrix, secondMatrix, result, start, end),
                    "Поток " + t
            );
            workers[t].start();
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.out.println("Error");
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return result;
    }

    public static void main(String[] args) {

        int s = 1000;
        int runs = 10;

        double[][] A = new double[s][s];
        double[][] B = new double[s][s];

        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                A[i][j] = Math.random() * 10;
                B[i][j] = Math.random() * 10;
            }
        }

        System.out.println("Размеры матриц: " + s + " x " + s);
        System.out.println();

        double totalSingleTime = 0;

        for (int t = 1; t <= runs; t++) {
            long start = System.nanoTime();
            double[][] result = multiply(A, B);
            long end = System.nanoTime();

            double timeMs = (end - start) / 1_000_000.0;
            totalSingleTime += timeMs;
        }

        double avgSingle = totalSingleTime / runs;
        System.out.println("Однопоточная реализация:");
        System.out.println("Время: " + avgSingle + "мс");
        System.out.println();

        double totalParallelTime = 0;

        for (int t = 1; t <= runs; t++) {
            long start = System.nanoTime();
            double[][] result = multiplyParallel(A, B);
            long end = System.nanoTime();

            double timeMs = (end - start) / 1_000_000.0;
            totalParallelTime += timeMs;
        }

        double avgParallel = totalParallelTime / runs;
        System.out.println("Многопоточная реализация:");
        System.out.println("Использовано потоков: " + THREAD_LIMIT);
        System.out.println("Время: " + avgParallel + " мс");
        System.out.println();

        System.out.println("Ускорение: " +
                String.format("%.2f", avgSingle / avgParallel) + "x");

    }
}
