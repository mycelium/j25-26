## HSAI 25-26 Java course - 1

### 1. Java: Matrix multiplication

- реализовать функцию `public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix);`
- Вывести время выполнения для матриц большой размерности
- Оптимизировать время выполнения (описать в README примененные подходы)

#### Алгоритм реализации умножения

Принцип оптимизации заключается в изменении порядка обхода циклов матрицы, что позволяет более эффективно работать с кэшем во время вычислений.

Стандартный подход предполагает обход: $i \rightarrow j \rightarrow k$:

```
for (int i = 0; i < rowsFirstMatrix; i++) {
    for (int j = 0; j < colsSecondMatrix; j++) {
        for (int k = 0; k < colsFirstMatrix; k++) {
            resultMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
        }
    }
}
```

В оптимизированном варианте обход происходит в следующем порядке: $i \rightarrow k \rightarrow j$:

```
for (int i = 0; i < rowsFirstMatrix; i++) {
    for (int k = 0; k < colsFirstMatrix; k++) {
        double value = firstMatrix[i][k];
        for (int j = 0; j < colsSecondMatrix; j++) {
            resultMatrix[i][j] += value * secondMatrix[k][j];
        }
    }
}
```

В данном случае внутренний цикл перебирает элементы массивов матриц подряд. Также, использование переменной позволяет использовать значения из первой матрицы без повторного обращения к ней.

#### Сравнение алгоритмов

Была выполнена проверка на нескольких размерах матриц. Для того, чтобы время выполнения приводилось наиболее точно, функция выполняется 20 раз, после чего результат выполнения складывается из усреднения результатов выполнения. 

$i \rightarrow j \rightarrow k$:
300 x 300: 69 ms;
500 x 500: 379 ms;
1000 x 1000: 6457 ms.

$i \rightarrow k \rightarrow j$:
300 x 300: 31 ms;
500 x 500: 101 ms;
1000 x 1000: 993 ms.