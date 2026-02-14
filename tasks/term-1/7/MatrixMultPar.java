import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMult {

	public static double[][] multiplyParallel(double[][] firstMatrix, double[][] secondMatrix, int threadsNum){
		try{
			long time = System.currentTimeMillis();
            if(firstMatrix[0].length != secondMatrix.length) throw new Error();
            int len1 = firstMatrix.length;
            int len2 = secondMatrix[0].length;
            int len3 = firstMatrix[0].length;
            double[][] res = new double[len1][];
            ExecutorService threadpool = Executors.newFixedThreadPool(threadsNum);

    		for(int i = 0; i < len1; i++){
				final int fi = i;
				threadpool.submit(() -> {
					double[] line = new double[len2];
					for(int j = 0; j < len2; j++){
						double sum = 0;
						for(int k = 0; k < len3; k++){
							sum+=firstMatrix[fi][k] * secondMatrix[k][j];
						}
						line[j] = sum;
					}
					res[fi] = line;
				});
    		}
			threadpool.shutdown();

			System.out.println("time: " + (System.currentTimeMillis() - time));

			try {
				if (!threadpool.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
					threadpool.shutdownNow();
				}
			} catch (InterruptedException e) {
				threadpool.shutdownNow();
				Thread.currentThread().interrupt();
			}

			return res;
        }
        catch(Exception e){
			e.printStackTrace();
        	return new double[1][1];
		}
	}

	public static void main(String[] args){
		double[][] A = {{1, 2}, {3, 2}};
        double[][] B = {{3, 1}, {4, 4}};
        double[][] C = {{11, 9}, {17, 11}};
        double[][] res = multiplyParallel(A, B, Math.min(A.length(), 20));
    
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
	}
}
