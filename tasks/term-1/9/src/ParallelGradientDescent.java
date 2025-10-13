package src;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParallelGradientDescent {

    private static final double X_MIN = -5.12;
    private static final double X_MAX = 5.12;

    public static double f(double x, double y) {
        double r = Math.sqrt(x * x + y * y);
        double numerator = 1.0 + Math.cos(12.0 * r);
        double denominator = 0.5 * r * r + 2.0;

        return -numerator / denominator;
    }

    private static double df_dr(double r) {
        if (r < 1e-12) return 0.0;

        double cos12r = Math.cos(12.0 * r);
        double sin12r = Math.sin(12.0 * r);
        double D = 0.5 * r * r + 2.0;
        double D2 = D * D;

        double numeratorDerivative = 12.0 * sin12r * D - r * (1.0 + cos12r);
        return numeratorDerivative / D2;
    }

    public static double[] gradient(double x, double y) {
        double r = Math.sqrt(x * x + y * y);

        if (r < 1e-12) {
            return new double[]{0.0, 0.0};
        }

        double dfdr = df_dr(r);
        double scale = dfdr / r;

        return new double[]{scale * x, scale * y};
    }

    public static double[] gradientDescent(double x, double y,
                                           double learningRate,
                                           int maxIter,
                                           double tolerance) {
        for (int i = 0; i < maxIter; i++) {
            double[] grad = gradient(x, y);
            double newX = x - learningRate * grad[0];
            double newY = y - learningRate * grad[1];

            double step = Math.sqrt(Math.pow(newX - x, 2) + Math.pow(newY - y, 2));
            if (step < tolerance) {
                x = newX;
                y = newY;
                break;
            }

            x = newX;
            y = newY;

            x = Math.max(X_MIN, Math.min(X_MAX, x));
            y = Math.max(X_MIN, Math.min(X_MAX, y));
        }
        return new double[]{x, y};
    }

    public static void main(String[] args) {
        int numThreads = 32;
        double learningRate = 0.005;
        int maxIter = 10000;
        double tolerance = 1e-12;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<double[]>> futures = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < numThreads; i++) {
            double startX = X_MIN + rand.nextDouble() * (X_MAX - X_MIN);
            double startY = X_MIN + rand.nextDouble() * (X_MAX - X_MIN);

            Future<double[]> future = executor.submit(() ->
                    gradientDescent(startX, startY, learningRate, maxIter, tolerance)
            );
            futures.add(future);
        }

        double bestX = 0, bestY = 0;
        double bestValue = Double.POSITIVE_INFINITY;

        for (Future<double[]> future : futures) {
            try {
                double[] point = future.get();
                double x = point[0], y = point[1];
                double val = f(x, y);

                if (val < bestValue) {
                    bestValue = val;
                    bestX = x;
                    bestY = y;
                }
            }
            catch (Exception e) {
                System.err.println("Error!");
            }
        }

        executor.shutdown();

        System.out.printf("Лучший результат: x = [%.8f, %.8f], f(x) = %.10f%n", bestX, bestY, bestValue);
        System.out.println("Истинный минимум: [0.0, 0.0], f = -1.0");
    }
}