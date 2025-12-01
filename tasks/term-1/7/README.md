### 7. Java: Parallel matrix multiplication

- реализовать функцию `public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix);`
- Реализация должна использовать многопоточность
- Вывести время выполнения для матриц большой размерности
- Сравнить время с однопоточной реализацией из лабораторной 1
- Подобрать оптимальное количество потоков под вашу платформу, описать подход к подбору в README
---
### Описание определения оптимального количества потоков
Для определения оптимального количества потоков была написана вспомогательная функция `void findOptimalThreadCount(double[][] firstMatrix, double[][] secondMatrix)`, которая для каждого количества потоков (от 1 до 12) проводит 10 тестов и определяет среднее время умножения матриц многопоточным способом. В итоге выводит оптимальное количество поток и его среднее время выполнения умножения. Код функции:
```
public static void findOptimalThreadCount(double[][] firstMatrix, double[][] secondMatrix) {
		int maxThreads = Runtime.getRuntime().availableProcessors();
		long bestTime = Long.MAX_VALUE;
		int optimalThreads = 1;
		int exps = 10;

		System.out.println("Максимальное количество потоков = " + maxThreads);

		for (int threads = 1; threads <= maxThreads; threads++) {
			numThreads = threads;
			long totalTime = 0;

			for (int i = 0; i < exps; i++) {
				long startTime = System.currentTimeMillis();
				multiplyParallel(firstMatrix, secondMatrix);
				long endTime = System.currentTimeMillis();
				totalTime += (endTime - startTime);
			}

			long avgTime = totalTime / exps;
			System.out.println("Потоков: " + threads + ", Среднее время: " + avgTime + " мс");

			if (avgTime < bestTime) {
				bestTime = avgTime;
				optimalThreads = threads;
			}
		}

		numThreads = optimalThreads;

		System.out.println("\n-------------------------------------------------------------");
		System.out
				.println("Оптимальное число потоков: " + optimalThreads + ", со средним временем: " + bestTime + " мс");
	}
```
Таким образом было определено, что оптимальным количеством потоков является 12.

### Результаты
По итогу многопоточная реализация умножения матриц справляется в 4.73 раза быстрее чем однопоточная.

|                  | Однопоточная | Многопоточная$
|------------------|--------------|---------------
| $1000 \cdot 1000$| 171 ms       | 47 ms
| $1500 \cdot 1500$| 1075 ms      | 222 ms
| $2000 \cdot 2000$| 2869 ms      | 504 ms