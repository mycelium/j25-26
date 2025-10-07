package lab1;


public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int rowsA = firstMatrix.length;
        int colsA = firstMatrix[0].length;
        int rowsB = secondMatrix.length;
        int colsB = secondMatrix[0].length;
        
        if (colsA != rowsB) {
            throw new IllegalArgumentException("Матрицы нельзя умножить");
        }
        
        double[][] result = new double[rowsA][colsB];
        
        // Умножение с оптимизацией - поменяла порядок циклов
        for (int i = 0; i < rowsA; i++) {
            for (int k = 0; k < colsA; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < colsB; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        // Проверка что умножение работает правильно
        double[][] A = {
            {1, 2, 3},
            {4, 5, 6}
        };
        
        double[][] B = {
            {7, 8},
            {9, 10},
            {11, 12}
        };
        
        System.out.println("Проверка работы:");
        double[][] result = multiply(A, B);
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }
        
        // Проверка корректности
        System.out.println("\nПроверка корректности:");
        boolean isCorrect = true;
        
        // Ожидаемые результаты
        double[][] expected = {
            {58.0, 64.0},
            {139.0, 154.0}
        };
        
        // Сравнение с ожидаемым результатом
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                if (Math.abs(result[i][j] - expected[i][j]) > 0.001) {
                    isCorrect = false;
                    System.out.printf("Ошибка! [%d][%d]: получено %.1f, ожидалось %.1f%n", 
                        i, j, result[i][j], expected[i][j]);
                }
            }
        }
        
        if (isCorrect) {
            System.out.println("Умножение работает корректно");
        } else {
            System.out.println("Есть ошибки в умножении");
        }
        
        // Измерение времени для больших матриц
        System.out.println("\nИзмерение времени:");
        
        int size = 1000;
        double[][] bigA = generateRandomMatrix(size, size);
        double[][] bigB = generateRandomMatrix(size, size);
        
        // Разогрев
        multiply(bigA, bigB);
        
        // Замер времени
        long start = System.nanoTime();
        multiply(bigA, bigB);
        long end = System.nanoTime();
        
        double time = (end - start) / 1_000_000.0;
        System.out.printf("Время для матриц %dx%d: %.2f мс%n", size, size, time);
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
}
