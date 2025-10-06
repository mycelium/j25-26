public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        try {
            if (firstMatrix == null || secondMatrix == null) {
                throw new IllegalArgumentException("Матрицы не могут быть null");
            }

            if(firstMatrix.length == 0 ||secondMatrix.length == 0) {
                throw new IllegalArgumentException("Матрицы не могут быть пустыми");
            }

            int rowsInA = firstMatrix.length;
            int rowsInB = secondMatrix.length;
            int colsInA = firstMatrix[0].length;
            int colsInB = secondMatrix[0].length;

            if(colsInA != rowsInB){
                throw new IllegalArgumentException(
                        String.format("Несовместимые размеры матриц: fristMatrix[%d][%d] и secondMatrix[%d][%d] - количество столбцов первой матрицы должно равняться количеству строк второй матрицы", rowsInA, colsInA, rowsInB, colsInB)
                );
            }

            double[][] result = new double[rowsInA][colsInB];

            for(int i = 0;i<rowsInA; i++) {
                for (int j = 0; j < colsInB; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < rowsInB; k++) {
                        sum += firstMatrix[i][k] * secondMatrix[k][j];
                    }
                    result[i][j] = sum;
                }
            }

            return result;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при умножении матриц:" + e.getMessage(), e);
        }
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        try {
            if (firstMatrix == null || secondMatrix == null) {
                throw new IllegalArgumentException("Матрицы не могут быть null");
            }

            if(firstMatrix.length == 0 ||secondMatrix.length == 0) {
                throw new IllegalArgumentException("Матрицы не могут быть пустыми");
            }

            int rowsInA = firstMatrix.length;
            int rowsInB = secondMatrix.length;
            int colsInA = firstMatrix[0].length;
            int colsInB = secondMatrix[0].length;

            if(colsInA != rowsInB){
                throw new IllegalArgumentException(
                        String.format("Несовместимые размеры матриц: fristMatrix[%d][%d] и secondMatrix[%d][%d] - количество столбцов первой матрицы должно равняться количеству строк второй матрицы", rowsInA, colsInA, rowsInB, colsInB)
                );
            }

            double[][] result = new double[rowsInA][colsInB];
            double[][] secondMatrixTransposed = transpose(secondMatrix);

            for(int i = 0;i<rowsInA; i++) {
                for (int j = 0; j < colsInB; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < rowsInB; k++) {
                        sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                    }
                    result[i][j] = sum;
                }
            }

            return result;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при умножении матриц:" + e.getMessage(), e);
        }
    }

    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random();
            }
        }

        return matrix;
    }

    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    public static void printMatrix(double[][] matrix, String title, int width, int precision) {
        if (matrix == null) {
            System.out.println(title + ": null");
            return;
        }

        if (matrix.length == 0) {
            System.out.println(title + ": []");
            return;
        }

        System.out.println(title + " [" + matrix.length + "x" + matrix[0].length + "]:");

        for (double[] doubles : matrix) {
            if (doubles == null) {
                System.out.println("  [null]");
                continue;
            }

            System.out.print("  [");
            for (int j = 0; j < doubles.length; j++) {
                System.out.printf("%" + width + "." + precision + "f", doubles[j]);
                if (j < doubles.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println(" ]");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int size = 2000;
        System.out.println("Генерация матрицы " + size + "x" + size + "...");

        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);

        int numOfExperiments = 10;
        long total = 0;

        System.out.println("Стандартный подход");
        for (int exp = 0; exp < numOfExperiments; exp++) {
            long start = System.currentTimeMillis();
            multiply(A, B);
            long end = System.currentTimeMillis();
            long duration = end - start;
            total += duration;

            System.out.println("Время выполнения для итерации " + (exp + 1) + " составило " + duration + " мс");
        }
        System.out.println("Усредненное время выполнения умножения стандартным подходом: " + (total/numOfExperiments) + " мс");
        System.out.println();

        total = 0;
        System.out.println("Оптимизированный подход");

        for (int exp = 0; exp < numOfExperiments; exp++) {
            long start = System.currentTimeMillis();
            multiplyOptimized(A, B);
            long end = System.currentTimeMillis();
            long duration = end - start;
            total += duration;

            System.out.println("Время выполнения для итерации " + (exp + 1) + " составило " + duration + " мс");
        }
        System.out.println("Усредненное время выполнения умножения оптимизированным подходом: " + (total/numOfExperiments) + " мс");
    }
}