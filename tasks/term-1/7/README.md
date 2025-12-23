## Параллельное умножение матриц
# Вычисление опитимального числа потоков

В коде метода поиска оптимального числа потоков приложены комментарии, поясняющие необходимые детали процесса подбора. 
```java
public static void findOptimalThreadCount(double[][] matrixA, double[][] matrixB, int iterations) {
        int maxThreads = Runtime.getRuntime().availableProcessors();//вычисляем максимальное число потоков для моей платформы
        long bestTime = Long.MAX_VALUE;
        int optimalThreads = 0;

        for (int threads = 1; threads <= maxThreads; threads++) {//для каждого числа потоков применяем функцию умножения
            num_Threads=threads;
            long totalTime = 0;

            for (int it = 0; it < iterations; it++) {//умножаем несколько раз, чтобы усреднить время
                long startTime = System.currentTimeMillis();
                multiplyParallel(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            long avgTime = totalTime / iterations;//вычисляем среднее время

            if (avgTime < bestTime) {//сравниваем с лучшим временем
                bestTime = avgTime;
                optimalThreads = threads;//меняем оптимальное число потоков
            }
        }
        num_Threads=optimalThreads;//присваиваем переменной числа потоков оптимальное значение
        System.out.println("Optimal thread count: " + optimalThreads);
        System.out.println("Best average time: " + bestTime + " ms");
    } 
```
Пример вывода для матрицы 1000x1000:

Optimal thread count: 10

Best average time: 35 ms

# Сравнение с однопоточной версией

Ниже приведены результаты сравнения для матрицы 1000x1000.

Average time for optimized mult: 140 ms

Average time for parallel mult: 36 ms

Вывод: параллельная реализация значительно ускоряет процесс умножения матриц больших размеров.