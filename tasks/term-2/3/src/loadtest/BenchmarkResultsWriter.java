package loadtest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

final class BenchmarkResultsWriter {
    private BenchmarkResultsWriter() {
    }

    static void write(Path resultsDir, BenchmarkRunConfig config, List<RunMetric> metrics) throws IOException {
        Files.createDirectories(resultsDir);
        Files.writeString(resultsDir.resolve("latest-detailed.csv"), detailedCsv(config, metrics), StandardCharsets.UTF_8);
        Files.writeString(resultsDir.resolve("latest-summary.csv"), summaryCsv(config, metrics), StandardCharsets.UTF_8);
    }

    private static String detailedCsv(BenchmarkRunConfig config, List<RunMetric> metrics) {
        StringBuilder csv = new StringBuilder();
        csv.append("variant,endpoint,repeat,server_threads,client_threads,preheat_requests,warmup_requests,requests,variant_order_seed,avg_ms,min_ms,p50_ms,p95_ms,p99_ms,throughput_rps,errors\n");
        for (RunMetric metric : metrics) {
            csv.append(escape(metric.variant().displayName())).append(',')
                    .append(escape(metric.endpoint().displayName())).append(',')
                    .append(metric.repeat()).append(',')
                    .append(config.serverThreads()).append(',')
                    .append(config.clientThreads()).append(',')
                    .append(config.preheatRequests()).append(',')
                    .append(config.warmupRequests()).append(',')
                    .append(metric.requests()).append(',')
                    .append(config.variantOrderSeed()).append(',')
                    .append(format(metric.avgMillis())).append(',')
                    .append(format(metric.minMillis())).append(',')
                    .append(format(metric.p50Millis())).append(',')
                    .append(format(metric.p95Millis())).append(',')
                    .append(format(metric.p99Millis())).append(',')
                    .append(format(metric.throughputRps())).append(',')
                    .append(metric.errors()).append('\n');
        }
        return csv.toString();
    }

    private static String summaryCsv(BenchmarkRunConfig config, List<RunMetric> metrics) {
        StringBuilder csv = new StringBuilder();
        csv.append("endpoint,variant,server_threads,client_threads,preheat_requests,warmup_requests,requests_per_repeat,repeats,variant_order_seed,avg_ms,min_ms,p50_ms,p95_ms,p99_ms,throughput_rps,total_errors\n");
        for (SummaryMetric summary : summarize(metrics)) {
            csv.append(escape(summary.endpoint().displayName())).append(',')
                    .append(escape(summary.variant().displayName())).append(',')
                    .append(config.serverThreads()).append(',')
                    .append(config.clientThreads()).append(',')
                    .append(config.preheatRequests()).append(',')
                    .append(config.warmupRequests()).append(',')
                    .append(config.requests()).append(',')
                    .append(config.repeats()).append(',')
                    .append(config.variantOrderSeed()).append(',')
                    .append(format(summary.avgMillis())).append(',')
                    .append(format(summary.minMillis())).append(',')
                    .append(format(summary.p50Millis())).append(',')
                    .append(format(summary.p95Millis())).append(',')
                    .append(format(summary.p99Millis())).append(',')
                    .append(format(summary.throughputRps())).append(',')
                    .append(summary.errors()).append('\n');
        }
        return csv.toString();
    }

    static List<SummaryMetric> summarize(List<RunMetric> metrics) {
        Map<String, List<RunMetric>> grouped = new TreeMap<>();
        for (RunMetric metric : metrics) {
            String key = metric.endpoint().name() + "|" + metric.variant().name();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(metric);
        }

        List<SummaryMetric> summaries = new ArrayList<>();
        for (List<RunMetric> group : grouped.values()) {
            group.sort(Comparator.comparingInt(RunMetric::repeat));
            BenchmarkEndpoint endpoint = group.get(0).endpoint();
            BenchmarkVariant variant = group.get(0).variant();
            double avg = group.stream().mapToDouble(RunMetric::avgMillis).average().orElse(0.0);
            double min = group.stream().mapToDouble(RunMetric::minMillis).min().orElse(0.0);
            double p50 = group.stream().mapToDouble(RunMetric::p50Millis).average().orElse(0.0);
            double p95 = group.stream().mapToDouble(RunMetric::p95Millis).average().orElse(0.0);
            double p99 = group.stream().mapToDouble(RunMetric::p99Millis).average().orElse(0.0);
            double throughput = group.stream().mapToDouble(RunMetric::throughputRps).average().orElse(0.0);
            int errors = group.stream().mapToInt(RunMetric::errors).sum();
            summaries.add(new SummaryMetric(endpoint, variant, avg, min, p50, p95, p99, throughput, errors));
        }

        summaries.sort(Comparator
                .comparing((SummaryMetric metric) -> metric.endpoint().ordinal())
                .thenComparing(metric -> metric.variant().ordinal()));
        return summaries;
    }

    static String format(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    private static String escape(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
