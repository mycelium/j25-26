import java.util.Scanner;

public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        int firstRows = firstMatrix.length;
        int firstColumns = firstMatrix[0].length;
        int secondRows = secondMatrix.length;
        int secondColumns = secondMatrix[0].length;

        double[][] result = new double[firstRows][secondColumns];

        for (int i = 0; i < firstRows; i++) {
            for (int j = 0; j < firstColumns; j++) {
                for (int k = 0; k < secondColumns; k++) {
                    result[i][k] += firstMatrix[i][j] * secondMatrix[j][k];
                }
            }
        }

        return result;
    }

    //ввод положительного числа
    public static int readPositiveInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Ошибка! Введите положительное число!");
                }
            } else {
                System.out.println("Ошибка! Введите целое число!");
                scanner.next(); //очищаем некорректный ввод
            }
        }
    }

    //оптимизация
    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int cols2 = secondMatrix[0].length;

        double[][] result = new double[rows1][cols2];

        int blockSize = 64;

        //блочное умножение
        for (int blockI = 0; blockI < rows1; blockI += blockSize) {
            for (int blockJ = 0; blockJ < cols2; blockJ += blockSize) {
                for (int blockK = 0; blockK < cols1; blockK += blockSize) {

                    //умножаем текущие блоки
                    for (int i = blockI; i < Math.min(blockI + blockSize, rows1); i++) {
                        for (int k = blockK; k < Math.min(blockK + blockSize, cols1); k++) {
                            for (int j = blockJ; j < Math.min(blockJ + blockSize, cols2); j++) {
                                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ввод с проверкой на положительные числа
        int firstRows = readPositiveInt(scanner, "Введите количество строк для первой матрицы: ");
        int firstColumns = readPositiveInt(scanner, "Введите количество столбцов для первой матрицы: ");
        int secondRows = readPositiveInt(scanner, "Введите количество строк для второй матрицы: ");
        int secondColumns = readPositiveInt(scanner, "Введите количество столбцов для второй матрицы: ");

        //проверка на возможность перемножения
        if (firstColumns != secondRows) {
            System.out.println("Ошибка! Матрицы такой размерности нельзя перемножить! Столбцов в первой матрице должно быть столько же, сколько строк во второй!");
            scanner.close();
            return;
        }

        double[][] matrix1 = new double[firstRows][firstColumns];
        double[][] matrix2 = new double[secondRows][secondColumns];

        //заполняем случайными значениями
        for (int i = 0; i < firstRows; i++) {
            for (int j = 0; j < firstColumns; j++) {
                matrix1[i][j] = Math.random();
            }
        }
        for (int i = 0; i < secondRows; i++) {
            for (int j = 0; j < secondColumns; j++) {
                matrix2[i][j] = Math.random();
            }
        }

        //обычное перемножение
        int runs = 10;
        long totalTime = 0;

        for (int run = 0; run < runs; run++) {
            long startTime = System.nanoTime();
            double[][] result = multiply(matrix1, matrix2);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;
            System.out.println((run + 1) + ": " + duration + " мс");
        }

        System.out.println("Среднее время за 10 перемножений: " + (totalTime / runs) + " мс");

        //оптимизированное
        totalTime = 0;

        for (int run = 0; run < runs; run++) {
            long startTime = System.nanoTime();
            double[][] result = multiplyOptimized(matrix1, matrix2);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            totalTime += duration;
            System.out.println((run + 1) + ": " + duration + " мс");
        }

        System.out.println("Среднее время за 10 оптимизированных перемножений: " + (totalTime / runs) + " мс");

        scanner.close();
    }
}