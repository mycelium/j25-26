public class MatrixMult {

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

    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix) {
        if (firstMatrix == null || secondMatrix == null) {
            throw new IllegalArgumentException("Матрицы null");
        }

        if (firstMatrix[0].length != secondMatrix.length) {
            throw new IllegalArgumentException("Матрицы нельзя умножать");
        }

        int x = firstMatrix.length;

        int y = secondMatrix[0].length;

        int z = firstMatrix[0].length;

        double[][] finalMatrix = new double[x][y];

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    finalMatrix[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }
        return finalMatrix;
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

    public static void print(double[][] matr)
    {
        for (int i = 0; i < matr.length; i++)
        {
            for (int j = 0; j < matr[0].length; j++)
            {
                System.out.printf("%8.2f", matr[i][j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args)
    {
        int testCount = 15;


        double[][] m1 = randomGener(500,500);

        double[][] m2 = randomGener(500,500);

        long timeUP = 0;

        long time = 0;

        for(int k = 0; k < testCount; k++) {

            long startTimeUP = System.currentTimeMillis();
            double[][] res = multiplyUP(m1, m2);
            long endTimeUP = System.currentTimeMillis();

            timeUP += (endTimeUP - startTimeUP);

            long startTime = System.currentTimeMillis();
            double[][] res2 = multiply(m1, m2);
            long endTime = System.currentTimeMillis();


            time += (endTime - startTime);
        }
        System.out.println("Среднее время выполнения: " + timeUP / testCount + " мс");

        System.out.println("Среднее время выполнения: " + time / testCount + " мс");

    }
}

