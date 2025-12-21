import java.util.ArrayList;
import java.util.List;

public class MatrixMultPar {

    private static int threadsCount = 1;

    public static int getThreadsCount() {
        return threadsCount;
    }

    public static void setThreadsCount(int count) {
        threadsCount = count;
    }


    public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix)
    {


        if (firstMatrix == null || secondMatrix == null)
        {
            throw new IllegalArgumentException("Матрицы null");
        }

        if (firstMatrix[0].length != secondMatrix.length)
        {
            throw new IllegalArgumentException("Матрицы нельзя умножать");
        }

        int x = firstMatrix.length;
        int y = secondMatrix[0].length;
        int z = firstMatrix[0].length;

        double[][] finalMatrixPar = new double[x][y];

        if (Math.min(threadsCount, x) <= 0) setThreadsCount(1);

        List<Thread> threass = new ArrayList<>();

        for (int threadId = 0; threadId < threadsCount; threadId++) {
            final int currentThreadId = threadId;

            Thread thread = new Thread(() -> {
                for (int i = currentThreadId; i < x; i += threadsCount) {
                    double[] resultRow = finalMatrixPar[i];
                    double[] firstRow = firstMatrix[i];

                    for (int k = 0; k < z; k++) {
                        double temp = firstRow[k];
                        double[] secondRow = secondMatrix[k];

                        for (int j = 0; j < y; j++) {
                            resultRow[j] += temp * secondRow[j];
                        }
                    }
                }
            });
            threass.add(thread);
            thread.start();
        }

        for (Thread thread : threass) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Поток был прерван", e);
            }
        }

        return finalMatrixPar;
    }

    // One lab code
    public static double[][] multiplyUP(double[][] firstMatrix, double[][] secondMatrix)
    {
        if (firstMatrix == null || secondMatrix == null)
        {
            throw new IllegalArgumentException("Матрицы null");
        }

        if (firstMatrix[0].length != secondMatrix.length)
        {
            throw new IllegalArgumentException("Матрицы нельзя умножать");
        }

        int x = firstMatrix.length;
        int y = secondMatrix[0].length;
        int z = firstMatrix[0].length;

        double[][] finalMatrixUP = new double[x][y];

        for(int i = 0; i < x; i++)
        {
            for(int k = 0; k < z; k++)
            {
                double temp = firstMatrix[i][k];
                for(int j = 0; j < y; j++)
                {
                    finalMatrixUP[i][j] += temp * secondMatrix[k][j];
                }
            }
        }
        return finalMatrixUP;
    }


    static double[][] randomGener(int size_i, int size_j)
    {
        double[][] matr = new double[size_i][size_j];

        for (int i = 0; i < size_i; i++)
        {
            for (int j = 0; j < size_j; j++)
            {
                matr[i][j] = Math.random() * 50;
            }
        }
        return matr;
    }
    // One lab codeEnd

    public static void main(String[] args)
    {
        int testCount = 15;

        int matrixWeight = 1000; // Размер матрицы

        double[][] m1 = randomGener(matrixWeight,matrixWeight);
        double[][] m2 = randomGener(matrixWeight,matrixWeight);
        long timeUP = 0;
        int[] threadCount = {2, 4, 8, 12, 16};
        long[] timePar = new long[threadCount.length];


        for(int k = 0; k < testCount; k++) {

            long startTimeUP = System.currentTimeMillis();
            double[][] res = multiplyUP(m1, m2);
            long endTimeUP = System.currentTimeMillis();

            timeUP += (endTimeUP - startTimeUP);



            for (int t = 0; t < threadCount.length; t++) {
                int threads = threadCount[t];

                setThreadsCount(threads);

                long startTime = System.currentTimeMillis();
                double[][] resPar = multiplyParallel(m1, m2);
                long endTime = System.currentTimeMillis();

                timePar[t] += (endTime - startTime);

            }
            System.out.println();
        }
        System.out.println("Среднее время выполнения: " + timeUP / testCount + " мс");

        for (int t = 0; t < threadCount.length; t++) {
            System.out.println("Потоков: " + threadCount[t] + " Среднее время выполнения: " + timePar[t] / testCount + " мс");
        }


    }
}
