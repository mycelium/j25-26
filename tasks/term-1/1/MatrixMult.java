import java.util.Random;

public class MatrixMult {
    public static double[][] multiply0(double[][] firstMatrix, double[][] secondMatrix){
        int s1 = firstMatrix.length;
        int s2 = firstMatrix[0].length;
        int s3 = secondMatrix[0].length;
        double[][] res = new double[s1][s3];
        for (int i = 0; i < s1; i++)
        {
            for (int j = 0; j < s2; j++)
            {
                double fMElem = firstMatrix[i][j];
                for (int k = 0; k < s3; k++)
                {
                    res[i][k] += fMElem * secondMatrix[j][k];
                }
            }
        }
        return res;
    }
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
        int s1 = firstMatrix.length;
        int s2 = firstMatrix[0].length;
        int s3 = secondMatrix[0].length;
        double[][] res = new double[s1][s3];
        int block = 256;
        for (int i = 0; i < s1; i += block)
        {
            for (int j = 0; j < s2; j += block)
            {
                for (int k = 0; k < s3; k += block)
                {
                    int iM = Math.min(s1, i + block);
                    int jM = Math.min(s2, j + block);
                    int kM = Math.min(s3, k + block);
                    for (int ib = i; ib < iM; ib++)
                    {
                        for (int jb = j; jb < jM; jb++)
                        {
                            double fMBlock = firstMatrix[ib][jb];
                            for (int kb = k; kb < kM; kb++)
                            {
                               res[ib][kb] += fMBlock * secondMatrix[jb][kb];
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
    public static void main(String[] args)
    {
        Random r = new Random();
        int size1 = 1200;
        int size2 = 1000;
        int size3 = 1500;
        double[][] matrix1 = new double[size1][size2];
        double[][] matrix2 = new double[size2][size3];
        for(int i = 0; i < size1; i++)
        {
            for(int j = 0; j < size2; j++)
            {
                matrix1[i][j] = r.nextDouble() * 9 + 1;
            }
        }
        for (int i = 0; i < size2; i++)
        {
            for(int j = 0; j < size3; j++)
            {
                matrix2[i][j] = r.nextDouble() * 9 + 1;
            }
        }
        long multTime = 0;
        long start = 0;
        long end = 0;
        int numTest = 20;
        for (int i = 0; i < numTest; i++)
        {
            start = System.currentTimeMillis();
            multiply0(matrix1, matrix2);
            end = System.currentTimeMillis();
            multTime += end - start;
        }
        double resTime = (double) multTime / (double) numTest;
        System.out.println("Время выполнения (обычный метод): " + resTime);
        multTime = 0;
        for (int i = 0; i < numTest; i++)
        {
            start = System.currentTimeMillis();
            multiply(matrix1, matrix2);
            end = System.currentTimeMillis();
            multTime += end - start;
        }
        resTime = (double) multTime / (double) numTest;
        System.out.println("Время выполнения (блочный метод): " + resTime);
    }
}