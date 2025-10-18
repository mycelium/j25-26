import java.util.Random;

public class MatrixMult {

	public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}
		if (firstMatrix.length > 0 && secondMatrix.length > 0) {
			if (firstMatrix[0].length != secondMatrix.length){
				throw new IllegalArgumentException("размер матриц не соответствует для перемножения");
			}
		}
		else {
			return new double[0][0];
		}

		double[][] resultMatrix = new double[firstMatrix.length][secondMatrix[0].length];

		// не оптимизированный способ:

		//проходимся по результирующей матрице для заполнения
		for (int i = 0;i<resultMatrix.length;i++){
			for (int j = 0;j<resultMatrix[0].length;j++){

				double buf = 0;
				//проходимся по строке либо столбцу
				for (int k=0; k < secondMatrix.length;k++){
						 double str = firstMatrix[i][k];
						 double row = secondMatrix[k][j];
						buf += firstMatrix[i][k] * secondMatrix[k][j];
				}
				resultMatrix[i][j]= buf;
			}
		}
		return resultMatrix;
	}


	public static double[][] multiplyOpt(double[][] firstMatrix, double[][] secondMatrix){
		if (firstMatrix == null || secondMatrix == null) {
			throw new IllegalArgumentException("Матрицы не могут быть null");
		}
		if (firstMatrix.length > 0 && secondMatrix.length > 0) {
			if (firstMatrix[0].length != secondMatrix.length){
				throw new IllegalArgumentException("размер матриц не соответствует для перемножения");
			}
		}
		else {
			return new double[0][0];
		}

		//транспонируем вторую матрицу для оптимизации, чтобы и там и там перемножать строки
		double[][] tSecondMatrix = new double[secondMatrix[0].length][secondMatrix.length];

		for (int i = 0; i < secondMatrix.length; i++) {
			for (int j = 0; j < secondMatrix[0].length; j++) {
				tSecondMatrix[j][i] = secondMatrix[i][j];
			}
		}

		double[][] resultMatrix = new double[firstMatrix.length][secondMatrix[0].length];



		//проходимся по результирующей матрице для заполнения
		for (int i = 0;i<resultMatrix.length;i++){
			for (int j = 0;j<resultMatrix[0].length;j++){

				double buf = 0;
				//проходимся по строкам
				for (int k=0; k < tSecondMatrix[0].length;k++){
					double str = firstMatrix[i][k];
					double row = tSecondMatrix[j][k];
					buf += firstMatrix[i][k] * tSecondMatrix[j][k];
				}
				resultMatrix[i][j]= buf;
			}
		}
		return resultMatrix;
	}



	public static void main(String[] args){
		Random rand = new Random();
//		double[][] firstMatrix = new double[][] {
//				{1,2,3,4},
//				{5,6,7,8},
//				{9,0,9,8}
//		};
		double[][] firstMatrix = new double[1000][1000];
		for (double[] str:firstMatrix){
			for (double a: str){
				a = rand.nextDouble(100);
			}
		}
//		double[][] secondMatrix = new double[][] {
//				{1,2,3},
//				{4,5,6},
//				{7,8,9},
//				{0,9,8},
//		};
		double[][] secondMatrix = new double[1000][1000];
		for (double[] str:secondMatrix){
			for (double a: str){
				a = rand.nextDouble(100);
			}
		}
		long startTime = System.currentTimeMillis();
		double[][] resultMatrix = multiply(firstMatrix, secondMatrix);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime; // время в миллисекундах
		System.out.println("Время выполнения: " + duration + " мс");
//		for(double[] s: resultMatrix){
//			for (double d :s){
//				System.out.print(d + "\t");
//			}
//			System.out.print("\n");
//		}

		startTime = System.currentTimeMillis();
		resultMatrix = multiplyOpt(firstMatrix, secondMatrix);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime; // время в миллисекундах
		System.out.println("Время выполнения с оптимизацией: " + duration + " мс");
//		for(double[] s: resultMatrix){
//			for (double d :s){
//				System.out.print(d + "\t");
//			}
//			System.out.print("\n");
//		}



	}
}


