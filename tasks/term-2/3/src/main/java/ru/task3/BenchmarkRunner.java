package ru.task3;

public class BenchmarkRunner {

    public static void run(
            String name,
            String endpoint,
            int requests,
            int threads
    ) throws Exception {

        long time =
                LoadClient.benchmark(
                        endpoint,
                        requests,
                        threads
                );

        System.out.println(
                name +
                        " -> avg: " +
                        (time / (double) requests) +
                        " ms"
        );
    }
}
