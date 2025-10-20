## HSAI 25-26 Java course - 1
____
### 1. Java: Matrix multiplication
- Реализовать функцию ```public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix)```;
- Вывести время выполнения для матриц большой размерности
- Оптимизировать время выполнения (описать в README примененные подходы)
### Реализация перемножения матриц
Классический метод реализует математическую формулу, где каждый элемент result[i][j] вычисляется как скалярное произведение i-й строки первой матрицы и j-го столбца второй матрицы

```Java
double[][] result = new double[rows1][cols2];

    for (int i = 0; i < rows1; i++) {
        for (int j = 0; j < cols2; j++) {
            for (int k = 0; k < cols1; k++) {
                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
            }
        }
    }
    return result;
```

Почему он меделенный: В памяти матрица хранится построчно, а при операции secondMatrix[k][j] доступ происходит к строкам при каждом k.

Решение: транспонирование. Посел транспонирования второй матрицы получаем secondMatrixTransposed[j][k] - теперь тоже последовательный доступ.


Вторым подходом в оптмизации было использование параллельных вычислений. В данном случае было оптимально использовать параллельные вычисления для строк матрицы, так как каждый поток может обрабатывать свою строку независимо от других. 
Для реализации параллельных вычислений использовался метод IntStream.range().parallel().forEach()
Итоговый метод эффективно свомещает в себе оба подхода оптимизации

```Java
public static double[][] multiplyLevelUp(double[][] firstMatrix, double[][] secondMatrix) {
        int rows1 = firstMatrix.length;
        int cols1 = firstMatrix[0].length;
        int rows2 = secondMatrix.length;
        int cols2 = secondMatrix[0].length;

        if (cols1 != rows2) {
            throw new IllegalArgumentException("Несовместимые размеры матриц: " +
                    cols1 + " != " + rows2);
        }

        double[][] result = new double[rows1][cols2];
        double[][] secondMatrixTransposed = transpose(secondMatrix);

        IntStream.range(0, rows1).parallel().forEach(i -> {
            for (int j = 0; j < cols2; j++) {
                double sum = 0;
                for (int k = 0; k < cols1; k++) {
                    sum += firstMatrix[i][k] * secondMatrixTransposed[j][k];
                }
                result[i][j] = sum;
            }
        });

        return result;
    }
```
## Результаты
Результаты выполнения умножения матриц 5 разных размеров классическим и оптимизированным способом представлены в таблице. Тесты проводились с чуетом разогрева JVM

| Размер            | Классический подход | Оптимизированный подход |
|-------------------|--------------------|-------------------------|
| 250 $\times$ 250  | 43 с               | 4 мс                    |
| 500 $\times$ 500  | 156 мс              | 23 мс                   |
| 1000 $\times$ 1000| 1415 мс             | 199 мс                  |
| 1500 $\times$ 1500| 7464 мс            | 714 мс                 |
| 2000 $\times$ 2000| 24189 мс            | 1541 мс                 |

Из таблицы видно, что подход с транспонированием второй матрицы заметно сокращает время выполнения умножения матриц.

