## Отчет по работе 7. Java: Parallel matrix multiplication
### Цель работы
---
- Реализовать вычисление произведения матриц, использующую многопоточность и найти время выполнения для матриц большой размерности;
- Сравнить время с однопоточной реализацией из лабораторной 1;
- Подобрать оптимальное количество потоков;
### Вычисление произведение
---
Класс **MatrixMultParTask** реализует интерфейс **Runnable** и выполняет умножение для диапазона строк от **startRow** до **endRow**

Код реализации класса **MatrixMultParTask** представлен ниже:

```java
class MatrixMultParTask implements Runnable {  
    ...
  
    @Override  
    public void run(){  
        int m = secondMatrix[0].length;  
        int p = secondMatrix.length;  
        for (int i = startRow; i < endRow; i++)  
            for (int k = 0; k < p; k++) {  
                double firstVal = firstMatrix[i][k];  
                for (int j = 0; j < m; j++)  
                    res[i][j] += firstVal * secondMatrix[k][j];  
            }  
    }  
}
```

В конечной функции матрица делится по строкам между потоками равномерно. Для синхронизации используется **join()** для ожидания завершения всех потоков.

Код функции **public static double[][] multiplyParallel(double[], double[], int)** представлен ниже:

```java
public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadsNumb) {  
  
    validateMatrixes(firstMatrix, secondMatrix);  
    if (threadsNumb < 1 || threadsNumb > Runtime.getRuntime().availableProcessors())  
        throw new IllegalArgumentException("Invalid threads number.");  
  
    int rowsNumb          = firstMatrix.length;  
    int threadSNeededNumb = Math.min(threadsNumb, rowsNumb);  
    int rowNmbPerThread   = rowsNumb / threadSNeededNumb;  
    double[][] res        = new double[rowsNumb][secondMatrix[0].length];  
    Thread[]   threads    = new Thread[threadSNeededNumb];  
  
    for (int iThread = 0; iThread < threadSNeededNumb; iThread += 1){  
        int curRow  = iThread * rowNmbPerThread;  
        int nextRow = iThread != threadSNeededNumb - 1  
                       ? curRow + rowNmbPerThread  
                       : rowsNumb;  
  
        Runnable r       = new MatrixMultParTask(firstMatrix, secondMatrix, res,  
                                                 curRow,      nextRow);  
        threads[iThread] = new Thread(r);  
        threads[iThread].start();  
    }  
  
    for(Thread thr : threads){  
        try {  
            thr.join();  
        } catch (InterruptedException e){  
            Thread.currentThread().interrupt();  
            throw new RuntimeException("Thread " + thr.getId() + " was interrupted.", e);  
        }  
    }  
    return res;  
}
```
### Сравнение время выполнения
---
Были проведены вычисления для матриц размером 500 x 500, 1000 x 1000, 2000 x 2000. Ниже приведены сравнения времени выполнения однопоточной и многопоточной реализаций.

| Размерность матрицы | Один поток (мс) | Многопоточность (мс) |
| ------------------- | --------------- | -------------------- |
| 500 x 500           | 38              | 10                   |
| 1000 x 1000         | 140             | 56                   |
| 2000 x 2000         | 3027            | 507                  |

### Способ нахождения оптимального количества потоков
---
Нахождение оптимального количества потоков производится по принципу вычисления среднего времени нахождения произведения матриц для каждого количества потоков вплоть до максимального возможного в для текущей платформы. Затем среди всего списка значений находится поток с минимальным среднем временем.

Для симуляции вычисления произведения матриц используются матрицы 1500 x 1500, заполненные случайными значениями. Вычисление производится при помощи функции **multiplyParallel**, описанной выше.

Для каждого потока для измерения среднего значения производится 8 измерений времени выполнения. Максимальное количество потоков для текущей системы вычисляется при помощи функции **Runtime.getRuntime().availableProcessors()**.

Код функции **private static int getOptThreadNumb()**, реализующей нахождение оптимального количества потоков представлен ниже:

```java
private static int getOptThreadNumb(){  
  
    int        size                 = 1500;  
    int        maxThreadsNumb       = Runtime.getRuntime().availableProcessors();  
    int        samplesNumbPerThread = 8;  
    double[][] A                    = generateMatrix(size);  
    double[][] B                    = generateMatrix(size);  
    List<Long> avgTimes             = new ArrayList<>();  
  
    for (int threadsNumb = 1; threadsNumb <= maxThreadsNumb; threadsNumb++) {  
        Long totalTime = 0L;  
        for (int i = 0; i < samplesNumbPerThread; i++) {  
            Long start = System.currentTimeMillis();  
            multiplyParallel(A, B, threadsNumb);  
            totalTime += System.currentTimeMillis() - start;  
        }  
        avgTimes.add(totalTime / samplesNumbPerThread);   
    }  
    return avgTimes.indexOf(Collections.min(avgTimes));  
}
```
### Вывод
---
Результаты сравнений указывают на то что многопоточная реализация значительно быстрее чем однопоточного аналога. Особенно разница начинает быть заметной с увеличением размера матриц.
