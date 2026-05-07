package loadtest;

record BenchmarkRunConfig(
        String host,
        int basePort,
        int serverThreads,
        int clientThreads,
        int warmupRequests,
        int requests,
        int repeats
) {
}
