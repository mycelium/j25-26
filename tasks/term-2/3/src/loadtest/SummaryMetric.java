package loadtest;

record SummaryMetric(
        BenchmarkEndpoint endpoint,
        BenchmarkVariant variant,
        double avgMillis,
        double minMillis,
        double p50Millis,
        double p95Millis,
        double p99Millis,
        double throughputRps,
        int errors
) {
}
