
### 1. Java: Matrix multiplication

- реализовать функцию `public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix);`
- Вывести время выполнения для матриц большой размерности
- Оптимизировать время выполнения (описать в README примененные подходы)
---
#### Реализация
Стандартный способ умножения матриц выглядит так:
```
for (int i = 0; i < result.length; i++) {
  for (int j = 0; j < result[0].length; j++) {
    for (int k = 0; k < firstMatrix[0].length; k++) {
      result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
    }
  }
}
```
Однако при таком подходе на каждой итерации мы обращаемся к разным строкам второй матрицы, которые могут быть в разных частях кэша.

Оптимизированный способ умножения матриц заключается в изменении порядка прохода по элементам: $i \rightarrow k \rightarrow j$.
```
for (int i = 0; i < firstMatrix.length; i++) {
  for (int k = 0; k < firstMatrix[0].length; k++) {
    double firstVal = firstMatrix[i][k];
    for (int j = 0; j < secondMatrix[0].length; j++) {
      result[i][j] += firstVal * secondMatrix[k][j];
    }
  }
}
```
В таком случае внутренний цикл проходит по последовательным элементам массивов матрицы.

#### Сравнение подходов
Для каждого подхода и размера матриц умножение выполняется по 10 раз, а время усредняется.

|             | $i \rightarrow j \rightarrow k$ | $k \rightarrow i \rightarrow j$
|-------------|---------|---------|
| $500 \cdot 500$   | 159 ms | 15 ms|
| $1000 \cdot 1000$| 1883 ms | 140 ms|
| $2000 \cdot 2000$| 40061 ms | 3158 ms|