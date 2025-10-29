
public class MatrixMult {

    //основная функция перемножения матриц
//    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
//        if (firstMatrix == null || secondMatrix == null ||
//                firstMatrix.length == 0 || secondMatrix.length == 0 ||
//                firstMatrix[0] == null || secondMatrix[0] == null) {
//            System.out.println("Одна из матриц пустая.");
//            return null;
//        }
//        if(firstMatrix[0].length != secondMatrix.length){
//            System.out.println("Количество столбцов первой матрицы не равно количеству строк второй матрицы, перемножение невозможно");
//            return null;
//        }
//        double[][] result = new double[firstMatrix.length][secondMatrix[0].length];
//        for(int i =0; i < firstMatrix.length; i++){
//            for(int j =0; j< secondMatrix[0].length; j++){
//                result[i][j] =0;
//                for(int k =0; k < firstMatrix[0].length; k++){
//                    result[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
//                }
//            }
//        }
//        return result;
//    }

    //оптимизированная
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
        if (firstMatrix == null || secondMatrix == null ||
                firstMatrix.length == 0 || secondMatrix.length == 0 ||
                firstMatrix[0] == null || secondMatrix[0] == null) {
            System.out.println("Одна из матриц пустая.");
            return null;
        }
        if(firstMatrix[0].length != secondMatrix.length){
            System.out.println("Количество столбцов первой матрицы не равно количеству строк второй матрицы, перемножение невозможно");
            return null;
        }
        double[][] result = new double[firstMatrix.length][secondMatrix[0].length];
        for (int i = 0; i < firstMatrix.length; i++) {
            for (int k = 0; k < firstMatrix[0].length; k++) {
                double temp = firstMatrix[i][k];
                for (int j = 0; j < secondMatrix[0].length; j++) {
                    result[i][j] += temp * secondMatrix[k][j];
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int s= 500;
        double[][] A = new double[s][s];
        double[][] B = new double[s][s];

        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                A[i][j] = Math.random()*10;
                B[i][j] = Math.random()*10;
            }
        }

        //для поиска среднего времени
        double totalTime = 0;
        for (int t = 1; t <= 10; t++) {
            long start = System.nanoTime();
            double[][] result = multiply(A, B);
            long end = System.nanoTime();

            double timeMs = (end - start) / 1_000_000.0;

            totalTime += timeMs;
        }

        double srtime = totalTime / 10;
        System.out.println(srtime);

        //вывод времени выполнения
//        long start = System.nanoTime();
//        double[][] result = multiply(A, B);
//        long end = System.nanoTime();
//
//        double time = (end - start) / 1_000_000.0;
//        System.out.println(time);
    }
}

