package lab7;

public class MatrixMultPar {
    
    // Однопоточное умножение
    public static double[][] multiply(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }
    
    // Оптимизированное параллельное умножение с транспонированием
    public static double[][] multiplyParallel(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        int threads = Runtime.getRuntime().availableProcessors();
        
        // Транспонируем матрицу B для лучшей локальности кэша
        double[][] BT = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                BT[j][i] = B[i][j];
            }
        }
        
        Thread[] workers = new Thread[threads];
        
        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            final int start = threadId * n / threads;
            final int end = (threadId == threads - 1) ? n : (threadId + 1) * n / threads;
            
            workers[t] = new Thread(new Runnable() {
                public void run() {
                    for (int i = start; i < end; i++) {
                        double[] Ai = A[i]; // Кэшируем строку A[i]
                        double[] Ci = C[i]; // Кэшируем строку C[i]
                        for (int j = 0; j < n; j++) {
                            double[] BTj = BT[j]; // Кэшируем строку BT[j]
                            double sum = 0;
                            for (int k = 0; k < n; k++) {
                                sum += Ai[k] * BTj[k];
                            }
                            Ci[j] = sum;
                        }
                    }
                }
            });
            workers[t].start();
        }
        
        for (int t = 0; t < threads; t++) {
            try {
                workers[t].join();
            } catch (InterruptedException e) {
                System.out.println("Поток прерван: " + e.getMessage());
            }
        }
        
        return C;
    }
    
    // Создание тестовой матрицы
    public static double[][] createTestMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() * 100; // Случайные числа
            }
        }
        return matrix;
    }
    
    // Проверка корректности умножения
    public static boolean checkResult(double[][] A, double[][] B, double[][] C) {
        int n = A.length;
        double[][] expected = multiply(A, B);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(expected[i][j] - C[i][j]) > 0.001) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // Основной метод
    public static void main(String[] args) {
        System.out.println("=== ПАРАЛЛЕЛЬНОЕ УМНОЖЕНИЕ МАТРИЦ ===");
        System.out.println("Доступно процессоров: " + Runtime.getRuntime().availableProcessors());
        
        // Тестируем разные размеры матриц
        int[] sizes = {200, 500, 1000};
        
        for (int size : sizes) {
            System.out.println("\n--- Размер матрицы: " + size + "x" + size + " ---");
            
            double[][] A = createTestMatrix(size);
            double[][] B = createTestMatrix(size);
            
            // Прогрев JVM (не измеряем время)
            for (int i = 0; i < 2; i++) {
                multiply(A, B);
                multiplyParallel(A, B);
            }
            
            // Однопоточный тест (среднее из 3 запусков)
            long singleTotalTime = 0;
            for (int i = 0; i < 3; i++) {
                long startTime = System.nanoTime();
                double[][] resultSingle = multiply(A, B);
                long endTime = System.nanoTime();
                singleTotalTime += (endTime - startTime);
                
                // Проверяем корректность
                if (!checkResult(A, B, resultSingle)) {
                    System.out.println("ОШИБКА: Неправильный результат в однопоточном режиме!");
                }
            }
            double avgSingleTime = singleTotalTime / (3 * 1000000.0);
            
            // Многопоточный тест (среднее из 3 запусков)
            long multiTotalTime = 0;
            for (int i = 0; i < 3; i++) {
                long startTime = System.nanoTime();
                double[][] resultMulti = multiplyParallel(A, B);
                long endTime = System.nanoTime();
                multiTotalTime += (endTime - startTime);
                
                // Проверяем корректность
                if (!checkResult(A, B, resultMulti)) {
                    System.out.println("ОШИБКА: Неправильный результат в многопоточном режиме!");
                }
            }
            double avgMultiTime = multiTotalTime / (3 * 1000000.0);
            
            System.out.printf("Однопоточное: %.2f мс\n", avgSingleTime);
            System.out.printf("Многопоточное: %.2f мс\n", avgMultiTime);
            
            if (avgMultiTime > 0) {
                double speedup = avgSingleTime / avgMultiTime;
                System.out.printf("Ускорение: %.2f раз\n", speedup);
            }
            
            // Даем JVM отдохнуть между тестами
            System.gc();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        
        // Поиск оптимального количества потоков
        System.out.println("\n=== ПОИСК ОПТИМАЛЬНЫХ ПОТОКОВ ===");
        findOptimalThreadCount();
    }
    
    // Метод для поиска оптимального количества потоков
    public static void findOptimalThreadCount() {
        int size = 1000;
        double[][] A = createTestMatrix(size);
        double[][] B = createTestMatrix(size);
        
        int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
        long bestTime = Long.MAX_VALUE;
        int bestThreadCount = 1;
        
        System.out.println("Тестируем от 1 до " + maxThreads + " потоков:");
        System.out.println("Размер тестовой матрицы: " + size + "x" + size);
        
        for (int threadCount = 1; threadCount <= maxThreads; threadCount++) {
            final int threads = threadCount;
            
            long totalTime = 0;
            int runs = 3;
            
            for (int run = 0; run < runs; run++) {
                // Транспонируем матрицу B
                double[][] BT = new double[size][size];
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        BT[j][i] = B[i][j];
                    }
                }
                
                double[][] C = new double[size][size];
                Thread[] workers = new Thread[threads];
                
                long startTime = System.nanoTime();
                
                // Запускаем потоки
                for (int t = 0; t < threads; t++) {
                    final int start = t * size / threads;
                    final int end = (t == threads - 1) ? size : (t + 1) * size / threads;
                    
                    workers[t] = new Thread(new Runnable() {
                        public void run() {
                            for (int i = start; i < end; i++) {
                                double[] Ai = A[i];
                                double[] Ci = C[i];
                                for (int j = 0; j < size; j++) {
                                    double[] BTj = BT[j];
                                    double sum = 0;
                                    for (int k = 0; k < size; k++) {
                                        sum += Ai[k] * BTj[k];
                                    }
                                    Ci[j] = sum;
                                }
                            }
                        }
                    });
                    workers[t].start();
                }
                
                // Ждем завершения
                for (int t = 0; t < threads; t++) {
                    try {
                        workers[t].join();
                    } catch (InterruptedException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                }
                
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
                
                System.gc();
                
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
            
            double avgTime = totalTime / (runs * 1000000.0);
            System.out.printf("  %2d потоков: %7.2f мс\n", threadCount, avgTime);
            
            if (totalTime < bestTime) {
                bestTime = totalTime;
                bestThreadCount = threadCount;
            }
        }
        
        System.out.println("\nРЕЗУЛЬТАТ:");
        System.out.println("Оптимальное количество потоков: " + bestThreadCount);
        System.out.printf("Лучшее время: %.2f мс\n", bestTime / (3 * 1000000.0));
        
        // Теоретический анализ
        System.out.println("\nТЕОРЕТИЧЕСКИЙ АНАЛИЗ:");
        System.out.println("Физические ядра: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Рекомендуемые потоки: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Обоснование: Для вычислительно сложных задач оптимально использовать");
        System.out.println("количество потоков, равное количеству физических ядер процессора.");
    }
}
