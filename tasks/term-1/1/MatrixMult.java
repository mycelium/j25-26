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
}

