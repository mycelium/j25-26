## HSAI 25-26 Java course - 1

### 7. Java: Parallel matrix multiplication

- Реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`.
- Реализация должна использовать многопоточность.
- Вывести время выполнения для матриц большой размерности.
- Сравнить время с однопоточной реализацией из лабораторной 1.
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README.

### Подход к подбору оптимального количества потоков
- Получение количества доступных процессоров.
```java
(Runtime.getRuntime().availableProcessors())
```
- Тестирование производительности для разного количества потоков.
```java
public static void findOptimalThreadCount(double[][] matrixA, double[][] matrixB, int numExperiments){
    ...
    int maxThreads = Runtime.getRuntime().availableProcessors();
    for (int threads = 1; threads <= maxThreads; threads++) {
            setNumThreads(threads);
            long totalTime = 0;
            
            for (int exp = 0; exp < numExperiments; exp++) {
                long startTime = System.currentTimeMillis();
                multiplyParallel(matrixA, matrixB);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }
            
            long avgTime = totalTime / numExperiments;
            System.out.println("Threads: " + threads + ", Average time: " + avgTime + " ms");
            
            if (avgTime < bestTime) {
                bestTime = avgTime;
                optimalThreads = threads;
            }
    }
    ...
}
```

### Сравнение времени
Для каждого размера матриц умножение выполняется 10 раз, а время усредняется.

Для моей платформы оптимальное количество потоков - 4. 
|             | Однопоточная релализация | Многопоточная реализация 
|-------------|---------|---------|
| $500 \cdot 500$   | 33 ms | 25 ms |
| $1000 \cdot 1000$| 447 ms | 139 ms |
| $2000 \cdot 2000$| 4212 ms | 1618 ms |

Как видно из данных таблицы применение многопоточности позволяет заметно уменьшить время, требуемое для умножения матриц.