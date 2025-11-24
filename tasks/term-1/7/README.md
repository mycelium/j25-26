## HSAI 25-26 Java course - 1

### 7. Java: Parallel matrix multiplication

- реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`
- Реализация должна использовать многопоточность
- Вывести время выполнения для матриц большой размерности
- Сравнить время с однопоточной реализацией из лабораторной 1
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README

#### Подбор оптимального числа потоков

Для подбора оптимального числа потоков была реализована функция следующая функция:

```
public static int findOptimalThreadCount(double[][] A, double[][] B, int maxThreads, int runs) {
		long bestTime = Long.MAX_VALUE;
		int bestThreads = 1;

		for (int threads = 1; threads <= maxThreads; threads++) {
			long totalTime = 0;
			for (int i = 0; i < runs; i++) {
				long start = System.currentTimeMillis();
				multiplyParallel(A, B, threads);
				long end = System.currentTimeMillis();
				totalTime += (end - start);
			}
			long avgTime = totalTime / runs;
			System.out.printf("Threads: %2d | Avg time: %5d ms%n", threads, avgTime);

			if (avgTime < bestTime) {
				bestTime = avgTime;
				bestThreads = threads;
			}
		}

		System.out.println("\nOptimal thread count: " + bestThreads + " (avg time: " + bestTime + " ms)");
		return bestThreads;
	}
```

#### Сравнение времени выполнения

По результатам функции, представленной выше, было подобрано оптимальное число потоков для моей платформы – 7 потоков.

Во время сравнения операции производились по 10 раз, а результат усреднялся.

Матрица 500x500:
    Однопоточная реализация: 25 ms
    Многопоточная реализация: 9 ms

Матрица 1000x1000:
    Однопоточная реализация: 185 ms
    Многопоточная реализация: 48 ms

Матрица 1500x1500:
    Однопоточная реализация: 667 ms
    Многопоточная реализация: 184 ms
