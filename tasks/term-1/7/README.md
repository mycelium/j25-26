## HSAI 25-26 Java course - 1

### 7. Java: Parallel matrix multiplication

- Реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`.
- Реализация должна использовать многопоточность.
- Вывести время выполнения для матриц большой размерности.
- Сравнить время с однопоточной реализацией из лабораторной 1.
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README.

### Подбор оптимального количества потоков
Для выполнения работы использовался процессор Intel i9 9900K, имеющий 8 ядер и 16 логических потоков.
Подбор оптимального количества потоков производится с помощью последовательного запуска 10 итераций умножения матриц для каждого количества потоков (от 1 до 16). Для этого используется следующая функция:
```java
public static void tune(double[][] X, double[][] Y, int trials) {
        int maxCores = java.lang.Runtime.getRuntime().availableProcessors();
        long fastest = java.lang.Long.MAX_VALUE;
        int best = 1;

        java.lang.System.out.println("\nFinding the optimal number of threads (from 1 to " + maxCores + "):");

        for (int thr = 1; thr <= maxCores; ++thr) {
            configureThreads(thr);
            long sum = 0L;

            for (int rep = 0; rep < trials; ++rep) {
                long t0 = java.lang.System.currentTimeMillis();
                compute(X, Y);
                long t1 = java.lang.System.currentTimeMillis();
                sum += (t1 - t0);
            }

            long avg = sum / trials;
            java.lang.System.out.println("\tThreads count: " + thr + ": " + avg + " ms");

            if (avg < fastest) {
                fastest = avg;
                best = thr;
            }
        }

        java.lang.System.out.println("\nResults of finding optimal:");
        java.lang.System.out.println("\tOptimal thread count: " + best);
        java.lang.System.out.println("\tAverage time with this number of threads: " + fastest + " ms");
    }
```

В ходе тестирования было определено что оптимальным количеством потоков для моей системы является 14 потоков.

### Сравнение времени
Ниже представлена таблица содержащая в себе сравнение скорости работы однопоточной и многопоточной реализаций для раных размеров исходных матриц. Из сравнения видно, что с помощью применения многопоточности можно кратно уменьшить время на выполнение умножения матриц.

| Матрица | Однопоточная | Многопоточная |
|-------------|---------|---------|
| $200 \times 200$   | 7 ms | 1 ms |
| $500 \times 500$| 111 ms | 4 ms |
| $1000 \times 1000$| 913 ms | 29 ms |
| $2000 \times 2000$| 7782 ms | 386 ms |

