В данном проекте реализована функция умножения матриц на языке Java с применением блочного метода. Данная оптимизация значительно улучшает производительность операций умножения, особенно при работе с большими матрицами.

Основное улучшение заключается в внедрении блочного умножения, при котором матрицы обрабатываются не целиком, а разбиваются на меньшие блоки. Это позволяет лучше использовать кэш процессора, что снижает количество обращений к памяти и повышает скорость выполнения операции.
Обрабатывая матрицы блоками, мы уменьшаем вероятность пропуска данных, которые уже загружены в кэш. Это особенно важно для матриц большого размера.


    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException("Неверные размеры матриц для умножения");
        }

        int m = firstMatrix.length;
        int n = firstMatrix[0].length;
        int p = secondMatrix[0].length;

        double[][] resultMatrix = new double[m][p];
        int blockSize = 256;

        for (int i = 0; i < m; i += blockSize) {
            for (int j = 0; j < p; j += blockSize) {
                for (int k = 0; k < n; k += blockSize) {
                    for (int bi = i; bi < Math.min(i + blockSize, m); bi++) {
                        for (int bj = j; bj < Math.min(j + blockSize, p); bj++) {
                            double sum = 0;
                            for (int bk = k; bk < Math.min(k + blockSize, n); bk++) {
                                sum += firstMatrix[bi][bk] * secondMatrix[bk][bj];
                            }
                            resultMatrix[bi][bj] += sum;
                        }
                    }
                }
            }
        }
        return resultMatrix;
    }







