# Matrix multiply 1


## 1. Классическая реализация

Формула классического умножения матриц:

$$
C_{ij} = \sum_{k=0}^{p-1} A_{ik} \cdot B_{kj}
$$

Код на Java:

```java
 public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
    int m = firstMatrix.length;
    int p = firstMatrix[0].length;
    int p2 = secondMatrix.length;
    int n = secondMatrix[0].length;
    if (p != p2) {
        throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
    }
    double[][] res = new double[m][n];
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            double sum = 0.0;
            for (int k = 0; k < p; k++) {
                sum += firstMatrix[i][k] * secondMatrix[k][j];
            }
            res[i][j] = sum;
        }
    }
    return res;
}
```
Принцип работы: простая реализация по формуле
## Блочное умножение

### Формула

Разбиваем матрицы на блоки размера `B×B`. Тогда для блока результата:

$$
C_{i:i+B-1, \, j:j+B-1} \;+=\; \sum_{k=k_0}^{k_0+B-1} 
A_{i:i+B-1, \, k:k+B-1} \cdot B_{k:k+B-1, \, j:j+B-1}
$$

где `i,j,k` — индексы начала блока, а `B` — размер блока (В = 64 - самый оптимальный размер).

### Код на Java

```java
public static double[][] multiply(double[][] A, double[][] B) {
            int m = A.length;
            if (m == 0) return new double[0][0];
            int p = A[0].length;
            int p2 = B.length;
            int n = B[0].length;
            if (p != p2) {
                throw new IllegalArgumentException("Wrong sizes: " + p + " and " + p2);
            }
            double[][] C = new double[m][n];
            final int blockSize = 64;
            for (int ii = 0; ii < m; ii += blockSize) {
                int iMax = Math.min(ii + blockSize, m);
                for (int kk = 0; kk < p; kk += blockSize) {
                    int kMax = Math.min(kk + blockSize, p);
                    for (int jj = 0; jj < n; jj += blockSize) {
                        int jMax = Math.min(jj + blockSize, n);

                        for (int i = ii; i < iMax; i++) {
                            double[] aRow = A[i];
                            double[] cRow = C[i];
                            for (int k = kk; k < kMax; k++) {
                                double aVal = aRow[k];
                                double[] bRow = B[k];
                                for (int j = jj; j < jMax; j++) {
                                    cRow[j] += aVal * bRow[j];
                                }
                            }
                        }

                    }
                }
            }
            return C;
        }
```
Оптимизация блочного умножения матриц заключается в том, что алгоритм разбивает большие матрицы на небольшие подматрицы — блоки фиксированного размера (64×64) — и выполняет умножение этих блоков целиком. Это позволяет работать с данными, которые помещаются в кэш процессора, что резко уменьшает количество кэш-промахов при обращении к элементам матрицы. В наивном тройном цикле элементы второй матрицы (B[k][j]) читаются по столбцам, и при больших матрицах процессору приходится часто обращаться к оперативной памяти, что сильно замедляет вычисления.

Блочное умножение решает эту проблему, так как операции выполняются над небольшими кусками данных, которые остаются в кэше во время всех внутренних вычислений блока. Каждая строка блока первой матрицы и каждая строка блока второй матрицы используются многократно, что улучшает локальность данных. В результате уменьшается время доступа к памяти, и алгоритм работает быстрее на больших матрицах.

Среднее время выполнения умножения разных реализаций при 10 повторениях:

## Классическое умножение:

Size: 50x50
Average time: 0.76657 ms

Size: 100x100
Average time: 0.91056 ms

Size: 500x500
Average time: 187.49631 ms

Size: 1000x1000
Average time: 1621.35224 ms

## Блочное умножение:

Size: 50x50
Average time: 0.5452 ms

Size: 100x100
Average time: 1.89876 ms

Size: 500x500
Average time: 35.05804 ms

Size: 1000x1000
Average time: 200.17328 ms
