public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		int numRowsFirst = firstMatrix.length;
		int numRowSecond = secondMatrix.length;
		int numColsFirst = firstMatrix[0].length;
		int numColsSecond = secondMatrix[0].length;

		if(numRowSecond != numColsFirst){
			 throw new IllegalArgumentException(
                "Несовместимые размеры матриц: " + numRowsFirst + "x" + numColsFirst + " и " + 
                numRowSecond + "x" + numColsSecond
            );
		}
        double[][] result = new double[numRowsFirst][numColsSecond];
        
		for(int i=0;i<numRowsFirst;i++){
		    for(int k=0; k<numRowSecond;k++){
				double temp = firstMatrix[i][k];
			    for(int j=0;j<numColsSecond;j++){
					result[i][j]+=temp*secondMatrix[k][j];
				}
			}
		}
		return result;
	}


public static double[][] generateRandomMatrix(int rows, int cols, int min, int max) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (Math.random() * (max - min + 1)) + min;
            }
        }
        return matrix;
}

public static void printMatrix(double[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%8.2f", matrix[i][j]);
            }
            System.out.println();
    }
}

public static void testSmallMatrix(){
	double[][] A = {
            {1, 0, 3},
            {2, 5, 7}
        };

	double[][] B = {
            {6, 5},
            {3, 15},
            {2, 1}
        };

    double[][] C = multiply(A, B);

	System.out.println("Result for testSmallMatrix:");
	printMatrix(C);
       
}

public static void testLargeMatrix(){
	int[][] sizes = {
            {500, 500, 500},
            {1000, 1000, 1000},
			{1500, 1500, 1500},
        };
       

        
         for (int[] size : sizes) {
            int rowsA = size[0];
            int colsA = size[1];
            int colsB = size[2];

            double[][] matrixA = generateRandomMatrix(rowsA, colsA,1,100);
            double[][] matrixB = generateRandomMatrix(colsA, colsB,1,100);
            
            
			long totalTime=0;
			for (int i = 0; i < 10; i++){
            long startTime = System.currentTimeMillis();
            double[][] result = multiply(matrixA, matrixB);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime+=duration;
            }
		    System.out.println("Average execution time : " + (totalTime / 10) + " ms");
		}

}
public static void main(String[] args) {
        
        testSmallMatrix(); //Проверка на корректность выплонения перемножения матриц, на примере небольших матриц
		testLargeMatrix();
    }
}