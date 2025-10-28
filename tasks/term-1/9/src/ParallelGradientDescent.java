package src;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelGradientDescent {

    private record Vector2D(double x1, double x2) {
        public double norm() { return Math.hypot(x1, x2); }
    }
    public record ThreadResult(Vector2D data, double fValue) { }

    private static final double X_MIN = -5.12;
    private static final double X_MAX = 5.12;

    private static final double EPSILON = 1e-12;

    private static final double TOLERANCE = 1e-12;
    private static final double TOLERANCE_SQUARED = TOLERANCE * TOLERANCE;

    private double learningRate;
    private int maxIterations;

    private final double optimalValue;

    private static double f(Vector2D x) {
        double r = x.norm();

        double numerator = 1.0 + Math.cos(12.0 * r);
        double denominator = 0.5 * r * r + 2.0;

        return -numerator / denominator;
    }

    private static double df_dr(double r) {
        if (r < EPSILON) { return 0.0; }

        double v = (0.5 * r * r + 2.0);
        double numerator = -12 * Math.sin(12 * r) * v - r * (Math.cos(12 * r) + 1);
        double denominator = v * v;

        return -numerator / denominator;
    }

    private static Vector2D gradient(Vector2D x) {
        double r = x.norm();
        if (r < EPSILON) { return new Vector2D(0.0, 0.0); }

        double dfdr = df_dr(r);
        return new Vector2D(dfdr * x.x1() / r, dfdr * x.x2() / r);
    }

    private ThreadResult gradientDescentThread(Vector2D x) {
        Vector2D resultX = x;
        for (int i = 0; i < maxIterations; i++) {
            Vector2D grad = gradient(resultX);
            double nX1 = resultX.x1() - learningRate * grad.x1();
            double nX2 = resultX.x2() - learningRate * grad.x2();

            double dx1 = nX1 - resultX.x1;
            double dx2 = nX2 - resultX.x2;
            if (dx1 * dx1 + dx2 * dx2 < TOLERANCE_SQUARED) {
                resultX = new Vector2D(nX1, nX2);
                break;
            }

            resultX = new Vector2D(
                    Math.clamp(nX1, X_MIN, X_MAX),
                    Math.clamp(nX2, X_MIN, X_MAX)
            );
        }

        return new ThreadResult(resultX, f(resultX));
    }

    public ParallelGradientDescent(double learningRate, int maxIterations, double optimalValue) {
        if (learningRate < 0) {
            throw new IllegalArgumentException("Learning rate must be non-negative");
        }
        if (maxIterations < 0) {
            throw new IllegalArgumentException("Iterations must be non-negative");
        }
        if (optimalValue != -1) {
            throw new IllegalArgumentException("Function used in gradient descent minimum is -1");
        }

        this.learningRate = learningRate;
        this.maxIterations = maxIterations;
        this.optimalValue = optimalValue;
    }

    public static ParallelGradientDescent createDefault() {
        return new ParallelGradientDescent(
                0.005,
                10000,
                -1
        );
    }

    public ThreadResult optimize(int parallelStarts, long timeoutMs) {
        if (parallelStarts <= 0) {
            throw new IllegalArgumentException("Number of parallel starts must be positive");
        }
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Timeout must be non-negative");
        }

        AtomicReference<ThreadResult> best = new AtomicReference<>(
                new ThreadResult(new Vector2D(0, 0), Double.POSITIVE_INFINITY)
        );
        AtomicBoolean shouldStop = new AtomicBoolean(false);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < parallelStarts && !shouldStop.get(); i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    Vector2D start = new Vector2D(
                            X_MIN + ThreadLocalRandom.current().nextDouble() * (X_MAX - X_MIN),
                            X_MIN + ThreadLocalRandom.current().nextDouble() * (X_MAX - X_MIN)
                    );

                    ThreadResult res = gradientDescentThread(start);

                    best.getAndUpdate(currentBest ->
                            (res.fValue() < currentBest.fValue()) ? res : currentBest
                    );

                    if (Math.abs(res.fValue() - optimalValue) < EPSILON) {
                        shouldStop.set(true);
                    }
                }, executor);

                futures.add(future);
            }

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                all.orTimeout(timeoutMs, TimeUnit.MILLISECONDS).join();
            }
            catch (CompletionException e) {
                if (e.getCause() instanceof TimeoutException) {
                    System.out.println("Optimization stopped due to timeout.");
                }
                else {
                    System.err.println("Unexpected error during optimization: " + e.getCause().getMessage());
                }
            }
            catch (Exception e) {
                System.err.println("Unexpected exception: " + e.getMessage());
            }

            shouldStop.set(true);

        }

        return best.get();
    }

    public double getLearningRate() { return learningRate; }
    public int getMaxIterations() { return maxIterations; }

    public void setLearningRate(double learningRate) { this.learningRate = learningRate; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }

    public static void main(String[] args) {
        ParallelGradientDescent optimizer = ParallelGradientDescent.createDefault();

        long start = System.currentTimeMillis();
        var result = optimizer.optimize(1000, 5000);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("""
            Optimization finished in %d ms.
            Best point: [%.8f, %.8f]
            f(x)      : %.12f
            Error     : %.2e
            """,
                elapsed,
                result.data().x1(), result.data().x2(),
                result.fValue(),
                Math.abs(result.fValue() - (-1.0))
        );
    }

}