public class MatrixMult {
    // обычное перемножение
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
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

    // оптимизированное перемножение
    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix){
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

        double[][] secondMatrixT = transpose(secondMatrix);

        for (int i = 0; i < rowsA; i++) {
            double[] rowA = firstMatrix[i];
            for (int j = 0; j < colsB; j++) {
                double[] rowBT = secondMatrixT[j];
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += rowA[k] * rowBT[k];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    // транспонирование матрицы
    private static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    // генерация случайной матрицы
    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    // проверка равенства матриц
    public static boolean areMatricesEqual(double[][] a, double[][] b, double epsilon) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    // тестирование
    public static void main(String[] args) {
        int[] sizes = {100, 200, 500, 1000};

        System.out.println("Сравнение производительности умножения матриц:");
        System.out.println("Размер\t\tОбычное \tОптимизированное");
        System.out.println("--------------------------------------------------------");

        for (int size : sizes) {
            System.out.print(size + "x" + size + "\t");

            // генерируем случайные матрицы
            double[][] A = generateRandomMatrix(size, size);
            double[][] B = generateRandomMatrix(size, size);

            // замеряем время для обычного перемножения
            long startTime = System.currentTimeMillis();
            double[][] result1 = multiply(A, B);
            long endTime = System.currentTimeMillis();
            long time1 = endTime - startTime;
            System.out.print(time1 + " мс\t\t\t\t");

            // замеряем время для оптимизированного перемножения
            startTime = System.currentTimeMillis();
            double[][] result2 = multiplyOptimized(A, B);
            endTime = System.currentTimeMillis();
            long time2 = endTime - startTime;
            System.out.print(time2 + " мс\n");

            // проверяем корректность
            if (!areMatricesEqual(result1, result2, 1e-10)) {
                System.out.println("Ошибка: результаты не совпадают!");
                continue;
            }
        }
    }
}
