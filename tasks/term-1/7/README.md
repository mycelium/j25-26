## HSAI 25-26 Java course - 1

### 7. Java: Parallel matrix multiplication

- Реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`.
- Реализация должна использовать многопоточность.
- Вывести время выполнения для матриц большой размерности.
- Сравнить время с однопоточной реализацией из лабораторной 1.
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README.



### Выбор оптимального количества потоков
Для выбора оптимального количества потоков был проведен ряд эксперементов,
при которых измерялось время выполнения функции multiplyParallel. Для 
исключения влияния ошибки каждый тест проводился 10 раз и бралось усредненное значение.

Очевидно, что оптимальное количество потоков будет зависеть от размера матриц, поэтому 
эксперимент проводился на 3 разных размерах: 500х500, 1000х1000, 2000х2000. Для числа
потоков от 2 до 16 (верхняя граница выбрана такой, так как дальше нее значения только 
возрастают).

```java
for (int size : new int[] {500, 1000, 2000}){
            int optimal = findOptimalNumberOfThreads(size, 10, 16);
            System.out.printf("Optimal number of threads for %dx%d matrix is: %d\n", size, size, optimal);
        }
```
И сама функция для проверки:
```java
public static int findOptimalNumberOfThreads(int size, int numberOfTests, int maxThreads) {
        double[][] firstMatrix = createMatrix(size, size, 1, 100);
        double[][] secondMatrix = createMatrix(size, size, 1, 100);

        long bestTime = Long.MAX_VALUE;
        int optimalNumberOfThreads = 2;

        for (int threads = 2; threads <= maxThreads; threads++) {
            setNumThreads(threads);

            long totalTime = 0;

            for (int testNum = 0; testNum < numberOfTests; testNum++) {
                try {
                    long startTime = System.currentTimeMillis();
                    multiplyParallel(firstMatrix, secondMatrix);
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    totalTime += duration;

//                    System.out.println("Execution time for iteration " +
//                            (testNum + 1) + " for parallel multiply: " + duration + " ms");
                }
                catch (ArithmeticException e) {
                    System.out.println("\nError occured: "  + e.getMessage());
                }
            }


            System.out.printf(
                    "%d threads; total %d; average %d\n",
                    threads, totalTime, (totalTime / numberOfTests)
            );

            if (totalTime < bestTime) {
                bestTime = totalTime;
                optimalNumberOfThreads = threads;
            }
        }

        return optimalNumberOfThreads;
}
```

По результатам, расположенным в файле threadsResults.txt, было выбрано оптимальное число -- 8.

### Сравнение результатов
Для каждого размера матриц умножение выполняется 10 раз, а время усредняется.
Количество потоков выбрано равное 8.
Полные результаты находятся в файле parallelResults.txt.

|                   | Однопоточная релализация | Многопоточная реализация 
|-------------------|--------------------------|--------------------------|
| $100 \cdot 100$   | 3 ms                     | 3 ms                     |
| $500 \cdot 500$   | 47 ms                    | 8 ms                     |
| $1000 \cdot 1000$ | 710 ms                   | 86 ms                    |
| $2000 \cdot 2000$ | 6077 ms                  | 1712 ms                  |

Как можно заметить результаты для больших матриц значительно лучше. Коэффициент 6077 / 1712 = 3.5497.