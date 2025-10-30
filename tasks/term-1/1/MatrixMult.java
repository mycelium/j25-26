public class MatrixMult {
    public static double[][] multiply(double[][] firstMatrix, double[][] secondMatrix){
        try{
			long time = System.currentTimeMillis();
            if(firstMatrix[0].length != secondMatrix.length) throw new Error();
            int len1 = firstMatrix.length;
            int len2 = secondMatrix[0].length;
            int len3 = firstMatrix[0].length;
            double[][] res = new double[len1][];
            
    		for(int i = 0; i < len1; i++){
    		    double[] line = new double[len2];
    		    for(int j = 0; j < len2; j++){
    		        int sum = 0;
    		        for(int k = 0; k < len3; k++){
    		            sum+=firstMatrix[i][k] * secondMatrix[k][j];
    		        }
    		        line[j] = sum;
    		    }
    		    res[i] = line;
    		}

			System.out.println("time: " + (System.currentTimeMillis() - time));
    		return res;
        }
        catch(Exception e){System.err.println("Uncorrect matrixes for multiplication.");}
        return new double[1][1];
	}

	public static void main(String[] args) {
    	double[][] A = {{1, 2}, {3, 2}};
        double[][] B = {{3, 1}, {4, 4}};
        double[][] C = {{11, 9}, {17, 11}};
        double[][] res = multiply(A, B);
    
        boolean ok = true;
    	for (int i = 0; i < C.length; i++) {
    		for (int j = 0; j < C[0].length; j++) {
    			if (res[i][j] - C[i][j] != 0) {
    				ok = false;
    			}
    		}
    	}
        System.out.print("test 1: ");
        System.out.println(ok);

        int size = 20;
        double[][] A2 = new double[size][size];
        double[][] B2 = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A2[i][j] = Math.random();
                B2[i][j] = Math.random();
            }
        }
        double[][] res2 = MatrixMult.multiply(A2, B2);
    }
}

