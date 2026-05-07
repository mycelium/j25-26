package loadtest;

import httpserver.HttpServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RunAllBenchmarks {
    private RunAllBenchmarks() {
    }

    public static void main(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        BenchmarkRunConfig config = new BenchmarkRunConfig(
                options.get("host", BenchmarkDefaults.HOST),
                options.getInt("base-port", BenchmarkDefaults.BASE_PORT),
                options.getInt("server-threads", BenchmarkDefaults.SERVER_THREADS),
                options.getInt("client-threads", BenchmarkDefaults.CLIENT_THREADS),
                options.getInt("warmup-requests", BenchmarkDefaults.WARMUP_REQUESTS),
                options.getInt("requests", BenchmarkDefaults.REQUESTS),
                options.getInt("repeats", BenchmarkDefaults.REPEATS)
        );
        Path runtimeDir = options.getPath("runtime-dir", Path.of("3", "runtime"));
        Path resultsDir = options.getPath("results-dir", Path.of("3", "results"));
        Path readme = options.getPath("readme", Path.of("3", "README.md"));

        Files.createDirectories(runtimeDir);
        List<RunMetric> metrics = new ArrayList<>();

        for (BenchmarkVariant variant : BenchmarkVariant.values()) {
            int port = config.basePort() + variant.ordinal();
            Path storeFile = runtimeDir.resolve("store-" + variant.fileName() + ".jsonl");
            Files.deleteIfExists(storeFile);

            System.out.println();
            System.out.println("Starting " + variant.displayName() + " on port " + port);
            HttpServer server = BenchmarkHttpApp.create(config.host(), port, config.serverThreads(), variant, runtimeDir);
            server.start();

            try {
                Thread.sleep(300L);
                for (BenchmarkEndpoint endpoint : BenchmarkEndpoint.values()) {
                    System.out.println("Warmup " + endpoint.displayName() + ": " + config.warmupRequests() + " requests");
                    LoadTestClient.ClientRunResult warmup = LoadTestClient.run(
                            config.host(),
                            port,
                            endpoint,
                            config.warmupRequests(),
                            config.clientThreads()
                    );
                    if (warmup.errors() > 0) {
                        throw new IllegalStateException("Warmup failed for " + variant.displayName()
                                + " " + endpoint.displayName() + ": errors=" + warmup.errors()
                                + ", firstError=" + warmup.firstError());
                    }

                    for (int repeat = 1; repeat <= config.repeats(); repeat++) {
                        System.out.println("Measure " + endpoint.displayName() + ", repeat " + repeat
                                + ": " + config.requests() + " requests");
                        LoadTestClient.ClientRunResult result = LoadTestClient.run(
                                config.host(),
                                port,
                                endpoint,
                                config.requests(),
                                config.clientThreads()
                        );
                        RunMetric metric = RunMetric.from(variant, endpoint, repeat, result);
                        metrics.add(metric);
                        System.out.println("  avg_ms=" + BenchmarkResultsWriter.format(metric.avgMillis())
                                + ", p95_ms=" + BenchmarkResultsWriter.format(metric.p95Millis())
                                + ", rps=" + BenchmarkResultsWriter.format(metric.throughputRps())
                                + ", errors=" + metric.errors());
                        if (metric.errors() > 0) {
                            throw new IllegalStateException("Measured run failed: errors=" + metric.errors()
                                    + ", firstError=" + result.firstError());
                        }
                    }
                }
            } finally {
                server.stop();
            }
        }

        BenchmarkResultsWriter.write(resultsDir, config, metrics);
        ReportWriter.write(readme, config, metrics);
        System.out.println();
        System.out.println("Results written to " + resultsDir.toAbsolutePath());
        System.out.println("README updated: " + readme.toAbsolutePath());
    }
}
