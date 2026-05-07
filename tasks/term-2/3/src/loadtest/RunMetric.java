package loadtest;

import java.util.Arrays;

record RunMetric(
        BenchmarkVariant variant,
        BenchmarkEndpoint endpoint,
        int repeat,
        int requests,
        int errors,
        double avgMillis,
        double minMillis,
        double p50Millis,
        double p95Millis,
        double p99Millis,
        double throughputRps
) {
    static RunMetric from(
            BenchmarkVariant variant,
            BenchmarkEndpoint endpoint,
            int repeat,
            LoadTestClient.ClientRunResult result
    ) {
        long[] latencies = result.latenciesNanos();
        Arrays.sort(latencies);

        long sum = 0L;
        for (long latency : latencies) {
            sum += latency;
        }

        double avgMillis = nanosToMillis((double) sum / Math.max(1, latencies.length));
        double minMillis = nanosToMillis(latencies.length == 0 ? 0L : latencies[0]);
        double p50Millis = nanosToMillis(percentile(latencies, 50.0));
        double p95Millis = nanosToMillis(percentile(latencies, 95.0));
        double p99Millis = nanosToMillis(percentile(latencies, 99.0));
        double throughput = result.requests() / (result.totalNanos() / 1_000_000_000.0);

        return new RunMetric(
                variant,
                endpoint,
                repeat,
                result.requests(),
                result.errors(),
                avgMillis,
                minMillis,
                p50Millis,
                p95Millis,
                p99Millis,
                throughput
        );
    }

    private static long percentile(long[] sorted, double percentile) {
        if (sorted.length == 0) {
            return 0L;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }

    private static double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }
}
