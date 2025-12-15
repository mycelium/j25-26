import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MatrixMultParTask implements Runnable {
    private double[][] firstMatrix;
    private double[][] secondMatrix;
    private double[][] res;
    private int        startRow;
    private int        endRow;

    public MatrixMultParTask(double[][] aFirstMatrix,
                             double[][] aSecondMatrix,
                             double[][] aRes,
                             int        aStartRow,
                             int        aEndRow)
    {
        firstMatrix  = aFirstMatrix;
        secondMatrix = aSecondMatrix;
        res          = aRes;
        startRow     = aStartRow;
        endRow       = aEndRow;
    }

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

public class MatrixMultPar {

    public static void main(String[] args) {
        int[] sizes       = {500,1000,2000};
        int   threadsNumb = getOptThreadNumb();
        System.out.printf("Optimal number of threads = %d\n", threadsNumb);

        for (int size : sizes) {
            System.out.printf("--------------------\nSize: %d\n\n", size);
            double[][] A = generateMatrix(size);
            double[][] B = generateMatrix(size);

            long start = System.currentTimeMillis();
            multiply(A, B);
            long singleThreadTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            multiplyParallel(A, B, threadsNumb);
            long parallelTime = System.currentTimeMillis() - start;

            System.out.printf("Single Thread = %d ms\nParallel Time = %d ms\n",
                              singleThreadTime, parallelTime);
        }
    }

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
            System.out.printf("Number of threads = %d, Avg time = %d ms\n",
                              threadsNumb, totalTime / samplesNumbPerThread);
        }
        return avgTimes.indexOf(Collections.min(avgTimes));
    }

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {
        validateMatrixes(firstMatrix, secondMatrix);

        int n = firstMatrix.length;
        int m = secondMatrix[0].length;
        int p = secondMatrix.length;
        double[][] res = new double[n][m];

        for (int i = 0; i < n; i++)
            for (int k = 0; k < p; k++) {
                double firstVal = firstMatrix[i][k];
                for (int j = 0; j < m; j++)
                    res[i][j] += firstVal * secondMatrix[k][j];
            }
        return res;
    }

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

    private static void validateMatrixes(double[][] firstMatrix, double[][] secondMatrix) throws IllegalArgumentException {
        if (firstMatrix == null || secondMatrix == null)
            throw new IllegalArgumentException("Argument cannot be null.");
        else if (hasDiffLengths(firstMatrix) || hasDiffLengths(secondMatrix))
            throw new IllegalArgumentException("One of the matrixes has rows with different amounts of elements.");
        else if (firstMatrix[0].length != secondMatrix.length)
            throw new IllegalArgumentException("Amount of columns in first matrix" +
                    firstMatrix[0].length +
                    " isn't the same as the amount of rows in the second one "
                    + secondMatrix.length);
    }

    private static double[][] generateMatrix(int size){
        double[][] res = new double[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                res[i][j] = Math.random() * 99;
            }
        return res;
    }

    private static boolean hasDiffLengths(double[][] m) {
        if (m.length != 0) {
            int validLength = m[0].length;
            for (double[] row : m)
                if (row.length != validLength) return true;
        }
        return false;
    }
}
