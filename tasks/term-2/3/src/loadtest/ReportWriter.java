package loadtest;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ReportWriter {
    private ReportWriter() {
    }

    static void write(Path readme, BenchmarkRunConfig config, List<RunMetric> metrics) throws IOException {
        Path parent = readme.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(readme, render(readme, config, metrics), StandardCharsets.UTF_8);
    }

    private static String render(Path readme, BenchmarkRunConfig config, List<RunMetric> metrics) {
        Map<String, SummaryMetric> summary = new LinkedHashMap<>();
        for (SummaryMetric metric : BenchmarkResultsWriter.summarize(metrics)) {
            summary.put(key(metric.endpoint(), metric.variant()), metric);
        }

        StringBuilder report = new StringBuilder();
        report.append("# Task 3 - Load testing report\n\n");
        report.append("This folder contains the benchmark project for task 3. It uses the HTTP server from task 2 (`2/src/httpserver`) and the JSON parser from task 1 (`1/library/json`). For comparison with a production parser I also added Gson 2.14.0.\n\n");
        report.append("The goal of the run was simple: keep the same HTTP workload and compare four server/parser combinations:\n");
        report.append("virtual threads + own parser, virtual threads + Gson, classic threads + own parser, classic threads + Gson.\n\n");

        report.append("## How to configure and launch\n\n");
        report.append("Run everything from the repository root. The scripts expect JDK 21+ and PowerShell.\n\n");
        report.append("```powershell\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\download-gson.ps1\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\build.ps1\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\run-suite.ps1\n");
        report.append("```\n\n");
        report.append("`download-gson.ps1` only downloads `gson-2.14.0.jar` into `3/lib`. If there is no network, put the jar there manually and start with `build.ps1`.\n\n");
        report.append("The main runner accepts the benchmark parameters as script arguments, for example:\n\n");
        report.append("```powershell\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\run-suite.ps1 -Requests 1000 -Repeats 2\n");
        report.append("```\n\n");
        report.append("After a full run the suite rewrites this README and updates:\n\n");
        report.append("- `3/results/latest-detailed.csv`\n");
        report.append("- `3/results/latest-summary.csv`\n\n");

        report.append("## Experiment description\n\n");
        report.append("For every variant the benchmark starts my HTTP server on a separate port and sends `POST` requests with `java.net.http.HttpClient`. The request body is always JSON with the same shape: request id, user id, category, timestamp, 40 numbers, nested details, and tags.\n\n");
        report.append("The benchmark has two endpoints:\n\n");
        report.append("- `Request-1`: parse JSON, write a compact event into a JSONL file, read the last line back from that file, and return a JSON response with a checksum. The append/read block is synchronized so the file stays valid under parallel load. This means the endpoint intentionally includes serialized file I/O, not only HTTP and JSON parsing.\n");
        report.append("- `Request-2`: parse JSON, read one item from a 1024-entry in-memory map, calculate a few values from the numeric array, and return JSON.\n\n");
        report.append("Before the measured part starts, the runner preheats each server/parser combination. For the measured run I use a fixed shuffled order (`seed = ").append(config.variantOrderSeed()).append("`) instead of the enum order, so the same variant is not always first or last. Request objects are built before the timer starts, so the reported latency does not include client-side JSON string construction.\n\n");

        report.append("## Hardware description\n\n");
        HardwareInfo hardware = HardwareInfo.capture(readme);
        report.append("| Component | Value |\n");
        report.append("|-----------|-------|\n");
        report.append("| CPU | ").append(hardware.cpu()).append(" |\n");
        report.append("| Logical processors | ").append(hardware.logicalProcessors()).append(" |\n");
        report.append("| RAM | ").append(hardware.ram()).append(" |\n");
        report.append("| GPU | not relevant for this benchmark |\n");
        report.append("| Disk | ").append(hardware.disk()).append(" |\n");
        report.append("| OS | ").append(System.getProperty("os.name")).append(' ').append(System.getProperty("os.version")).append(" |\n");
        report.append("| JDK | ").append(System.getProperty("java.version")).append(" |\n\n");

        report.append("## Experiment parameters\n\n");
        report.append("| Parameter | Value |\n");
        report.append("|-----------|-------|\n");
        report.append("| Host | `").append(config.host()).append("` |\n");
        report.append("| Base port | `").append(config.basePort()).append("` |\n");
        report.append("| Server threads | `").append(config.serverThreads()).append("` |\n");
        report.append("| Client threads | `").append(config.clientThreads()).append("` |\n");
        report.append("| JVM preheat requests | `").append(config.preheatRequests()).append("` per endpoint/variant |\n");
        report.append("| Warmup requests | `").append(config.warmupRequests()).append("` per endpoint/variant |\n");
        report.append("| Measured requests | `").append(config.requests()).append("` per repeat |\n");
        report.append("| Repeats | `").append(config.repeats()).append("` |\n");
        report.append("| Variant order seed | `").append(config.variantOrderSeed()).append("` |\n");
        report.append("| Measured variant order | `").append(RunAllBenchmarks.displayOrder(RunAllBenchmarks.shuffledVariants(config.variantOrderSeed()))).append("` |\n");
        report.append("| Payload numbers | `40` numeric values per request |\n");
        report.append("| Load generator | custom Java client using `java.net.http.HttpClient` |\n\n");

        report.append("## Resulting table\n\n");
        report.append("Average client-side latency per request, in milliseconds. The table uses the average of ").append(config.repeats()).append(" measured repeats.\n\n");
        report.append("| req | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |\n");
        report.append("|-----|----------------------|----------------|----------------------|----------------|\n");
        for (BenchmarkEndpoint endpoint : BenchmarkEndpoint.values()) {
            report.append("| ").append(endpoint.displayName()).append(' ');
            for (BenchmarkVariant variant : BenchmarkVariant.values()) {
                report.append("| ").append(tableValue(summary.get(key(endpoint, variant)))).append(' ');
            }
            report.append("|\n");
        }
        report.append('\n');

        report.append("Detailed CSV: `3/results/latest-detailed.csv`.\n");
        report.append("Summary CSV: `3/results/latest-summary.csv`.\n");
        report.append("Last full run: `").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("`.\n");
        return report.toString();
    }

    private static String key(BenchmarkEndpoint endpoint, BenchmarkVariant variant) {
        return endpoint.name() + "|" + variant.name();
    }

    private static String tableValue(SummaryMetric metric) {
        if (metric == null) {
            return "not run";
        }
        return BenchmarkResultsWriter.format(metric.avgMillis()) + " ms";
    }

    private record HardwareInfo(String cpu, int logicalProcessors, String ram, String disk) {
        static HardwareInfo capture(Path readme) {
            return new HardwareInfo(
                    cpuName(),
                    Runtime.getRuntime().availableProcessors(),
                    totalMemory(),
                    diskInfo(readme)
            );
        }

        private static String cpuName() {
            String processor = System.getenv("PROCESSOR_IDENTIFIER");
            if (processor != null && !processor.isBlank()) {
                if (processor.contains("AuthenticAMD") && processor.contains("Family 25") && processor.contains("Model 80")) {
                    return "AMD Ryzen 5 5600H";
                }
                return processor;
            }
            return System.getProperty("os.arch", "not available");
        }

        private static String totalMemory() {
            try {
                java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                if (osBean instanceof com.sun.management.OperatingSystemMXBean extendedBean) {
                    return formatBytes(extendedBean.getTotalMemorySize());
                }
            } catch (SecurityException ignored) {
                // Report generation should not fail when hardware details are unavailable.
            }
            return "not available";
        }

        private static String diskInfo(Path readme) {
            try {
                Path anchor = existingAnchor(readme.toAbsolutePath());
                FileStore fileStore = Files.getFileStore(anchor);
                return formatBytes(fileStore.getTotalSpace()) + " on " + fileStore.name();
            } catch (IOException | SecurityException exception) {
                return "not available";
            }
        }

        private static Path existingAnchor(Path path) {
            Path current = path;
            while (current != null && !Files.exists(current)) {
                current = current.getParent();
            }
            return current == null ? Path.of(".").toAbsolutePath() : current;
        }

        private static String formatBytes(long bytes) {
            double gib = bytes / 1024.0 / 1024.0 / 1024.0;
            return String.format(Locale.US, "%.1f GiB", gib);
        }
    }
}
