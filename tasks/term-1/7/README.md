### 7. Java: Parallel matrix multiplication

- реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`
- Реализация должна использовать многопоточность
- Вывести время выполнения для матриц большой размерности
- Сравнить время с однопоточной реализацией из лабораторной 1
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README

---

#### Подбор оптимального количества потоков

Моя платформа имеет процессор Intel Core i7-1165G7; ядер: 4, логических процессоров (потоков): 8.
Подбор количества потоков выбирается в зависимости от размера матрицы: если матрица больше размеров 2000x2000, то задается максимальное количество потоков:


```java
private static int NUM_THREADS = Runtime.getRuntime().availableProcessors();
```

Если же размеры меньше, то вызывается отдельный метод, который "подбирает" подходящее количество потоков методом тестирования. Для этого создаются матрицы случайного размера, а затем происходит по 5 тестовых умножений с разным количеством потоков. То количество, которое показало наименьшее среднее время - используется для дальнейшего умножения.

```java
public static int calculateOptimalThreads(int size) {
        if (size >= 2000) { return NUM_THREADS; }
        int maxTestThreads = 2 * NUM_THREADS;
        int repeat = 5;
        double bestTime = Double.MAX_VALUE;
        int optimalThreads = 1;

        double[][] A = generateMatrix(size, size);
        double[][] B = generateMatrix(size, size);
        multiplyParallel(A, B);

        for (int threads = 1; threads <= maxTestThreads; threads++) {
            NUM_THREADS = threads;
            long totalTime = 0;
            for (int i = 0; i < repeat; i++) {
                long startTime = System.currentTimeMillis();
                multiplyParallel(A, B);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            double avgTime = (double) totalTime / repeat;
            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = threads;
            }
        }
        return optimalThreads;
    }
```
То количество, которое показало наименьшее среднее время - используется для дальнейшего умножения. Причем количество потоков может изменяться, так как на малых матрицах затраты на создание и переключение потоков могут быть сопоставимы с самим вычислительным временем.

#### Сравнение времени умножения

Сравнение времен выполнения умножения матриц представлено в таблице (умножение происходит 10 раз на разных матрицах одинаковой размерности, итоговое время - усредняется):

|             |  NonParal  |   Paral   |   Threads   |
|-------------|------------|-----------|-----------|
| 100 * 100   | 0.7 ms     | 0.4 ms    | 4         |
| 500 * 500   | 20.8 ms    | 7.3 ms    | 8         |
| 1000 * 1000 | 165.6 ms   | 61.4 ms   | 8         |
| 2000 * 2000 | 2541.2 ms  | 946.5 ms  | 8         |