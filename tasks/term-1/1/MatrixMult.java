public class MatrixMult {

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        int aRows = firstMatrix.length;
        int aCols = firstMatrix[0].length;
        int bRows = secondMatrix.length;
        int bCols = secondMatrix[0].length;
        
        if (aCols != bRows) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " + 
                aCols + " != " + bRows);
        }
        
        double[][] result = new double[aRows][bCols];
        
        for (int i = 0; i < aRows; i++) {
            for (int k = 0; k < aCols; k++) {
                double aik = firstMatrix[i][k];
                for (int j = 0; j < bCols; j++) {
                    result[i][j] += aik * secondMatrix[k][j];
                }
            }
        }
        
        return result;
    }

    public static double[][] multiplyOptimized(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы не могут быть null");
        }
        
        // ДОБАВЛЕНО: объявление переменных
        int aRows = firstMatrix.length;
        int aCols = firstMatrix[0].length;
        int bRows = secondMatrix.length;
        int bCols = secondMatrix[0].length;
        
        if (aCols != bRows) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " + 
                aCols + " != " + bRows);
        }
        
        // ДОБАВЛЕНО: объявление result
        double[][] result = new double[aRows][bCols];
        
        final int BLOCK_SIZE = 4;
        
        for (int i = 0; i < aRows; i++) {
            for (int k = 0; k < aCols; k++) {
                double aik = firstMatrix[i][k];
                int j = 0; // Инициализация j
                
                // Развертывание цикла по j
                for (; j <= bCols - BLOCK_SIZE; j += BLOCK_SIZE) {
                    result[i][j]     += aik * secondMatrix[k][j];
                    result[i][j + 1] += aik * secondMatrix[k][j + 1];
                    result[i][j + 2] += aik * secondMatrix[k][j + 2];
                    result[i][j + 3] += aik * secondMatrix[k][j + 3];
                }
                
                // Обработка оставшихся элементов
                for (; j < bCols; j++) {
                    result[i][j] += aik * secondMatrix[k][j];
                }
            }
        }
        
        return result;
    }

    public static void main(String[] args) {
        int[] sizes = {100, 500, 1000, 2000};
        
        System.out.println("Запуск тестирования производительности...");
        System.out.println("| Размер матрицы | Базовая версия | Оптимизированная версия | Ускорение |");
        System.out.println("|----------------|----------------|-------------------------|-----------|");
        
        for (int size : sizes) {
            System.out.println("Генерация матриц " + size + "x" + size + "...");
            double[][] matrixA = generateRandomMatrix(size, size);
            double[][] matrixB = generateRandomMatrix(size, size);
            
            // Прогрев JVM (3 итерации)
            for (int i = 0; i < 3; i++) {
                multiply(matrixA, matrixB);
                multiplyOptimized(matrixA, matrixB);
            }
            
            // Замер базовой версии (3 прогона, берем среднее)
            long baseTime = 0;
            for (int i = 0; i < 3; i++) {
                long startTime = System.currentTimeMillis();
                multiply(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                baseTime += (endTime - startTime);
            }
            baseTime /= 3;
            
            // Замер оптимизированной версии (3 прогона, берем среднее)
            long optimizedTime = 0;
            for (int i = 0; i < 3; i++) {
                long startTime = System.currentTimeMillis();
                multiplyOptimized(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                optimizedTime += (endTime - startTime);
            }
            optimizedTime /= 3;
            
            double speedup = (double) baseTime / optimizedTime;
            
            System.out.printf("| %dx%-12d | %-14d | %-23d | %.2fx      |%n", 
                size, size, baseTime, optimizedTime, speedup);
        }
    }
    
    private static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }
    
    private static boolean matricesEqual(double[][] a, double[][] b, double tolerance) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }
}