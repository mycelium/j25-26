import java.util.*;

public class MatrixMult {

    interface Multiplier {
        String name();
        double[][] multiply(double[][] A, double[][] B);
    }

    static class Naive implements Multiplier {
		@Override public String name() { return "Naive"; }
		@Override public double[][] multiply(double[][] A, double[][] B) {
			int n = A.length, a = A[0].length, b = B[0].length;

			double[][] C = new double[n][b];

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < b; j++) {
					double c = 0;
					for (int k = 0; k < a; k++){
						c += A[i][k] * B[k][j];
					}
					C[i][j] = c;
				}
			}
			return C;
		}
	}

    static class Transposed implements Multiplier {
        @Override public String name() { return "Transposed"; }

        @Override public double[][] multiply(double[][] A, double[][] B) {
            int n = A.length, a = A[0].length, b = B[0].length;
            double[][] BT = new double[b][a];
            for (int i = 0; i < a; i++)
                for (int j = 0; j < b; j++)
                    BT[j][i] = B[i][j];

            double[][] C = new double[n][b];
            for (int i = 0; i < n; i++) {
                double[] Ai = A[i];
                for (int j = 0; j < b; j++) {
                    double[] BTj = BT[j];
                    double c = 0;
                    for (int k = 0; k < a; k++)
                        c += Ai[k] * BTj[k];
                    C[i][j] = c;
                }
            }
            return C;
        }
    }

	static final class ParallelNaive implements Multiplier {
		@Override public String name() { return "ParallelNaive"; }

		@Override public double[][] multiply(double[][] A, double[][] B) {
			int n = A.length, a = A[0].length, b = B[0].length;

			double[][] C = new double[n][b];

			java.util.stream.IntStream.range(0, n).parallel().forEach(i -> {
				double[] Ai = A[i];
				double[] Ci = C[i];
				for (int j = 0; j < b; j++) {
					double c = 0;
					for (int k = 0; k < b; k++) {
						c += Ai[k] * B[k][j];
					}
					Ci[j] = c;
				}
			});

			return C;
		}
	}


	public static void main(String[] args) {
		final int N = 1000, M = 2000, K = 500;
		final int REPEATS = 10;

		Random rand = new Random();

		double[][] A = new double[N][M];
		double[][] B = new double[M][K];
		for (int i = 0; i < N; i++)
			for (int j = 0; j < M; j++)
				A[i][j] = rand.nextDouble() * 2 - 1;
		for (int i = 0; i < M; i++)
			for (int j = 0; j < K; j++)
				B[i][j] = rand.nextDouble() * 2 - 1;

		List<Multiplier> suite = List.of(
			new Naive(),
			new Transposed(),
			new ParallelNaive()
		);

		System.out.printf("Benchmark: A(%dx%d) * B(%dx%d); %d runs per algo%n", N, M, M, K, REPEATS);

		for (Multiplier algo : suite) {
			long totalNs = 0;

			for (int r = 0; r < REPEATS; r++) {
				long t0 = System.nanoTime();
				algo.multiply(A, B);
				long dt = System.nanoTime() - t0;
				totalNs += dt;
			}

			double totalSec = totalNs / 1_000_000_000.0;
			System.out.printf(Locale.US, "[%s] total=%.3f s for %d runs%n",
					algo.name(), totalSec, REPEATS);
		}

	}
}
