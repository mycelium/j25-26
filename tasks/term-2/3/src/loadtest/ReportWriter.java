package loadtest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ReportWriter {
    private ReportWriter() {
    }

    static void write(Path readme, BenchmarkRunConfig config, List<RunMetric> metrics) throws IOException {
        Path parent = readme.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(readme, render(config, metrics), StandardCharsets.UTF_8);
    }

    private static String render(BenchmarkRunConfig config, List<RunMetric> metrics) {
        Map<String, SummaryMetric> summary = new LinkedHashMap<>();
        for (SummaryMetric metric : BenchmarkResultsWriter.summarize(metrics)) {
            summary.put(key(metric.endpoint(), metric.variant()), metric);
        }

        StringBuilder report = new StringBuilder();
        report.append("# Term 2 / Task 3: Load Testing Report\n\n");
        report.append("This is a separate plain Java project for measuring the HTTP server from `lab-2` and the JSON parser from `lab-1`.\n");
        report.append("The experiment compares virtual/classic server threads and the custom JSON parser/Gson.\n\n");

        report.append("## How to configure and launch\n\n");
        report.append("Requirements: JDK 21+, PowerShell, Maven Central access for downloading Gson.\n\n");
        report.append("```powershell\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\download-gson.ps1\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\build.ps1\n");
        report.append("powershell -ExecutionPolicy Bypass -File .\\3\\scripts\\run-suite.ps1\n");
        report.append("```\n\n");
        report.append("If the network is unavailable, place `gson-2.14.0.jar` into `3/lib/` manually and run `build.ps1`.\n");
        report.append("Gson version: `2.14.0`, Maven Central: https://central.sonatype.com/artifact/com.google.code.gson/gson/2.14.0\n\n");

        report.append("## Experiment description\n\n");
        report.append("For each of the 4 variants, the suite starts the server on a dedicated port and the Java load generator sends `POST` requests through `java.net.http.HttpClient`.\n");
        report.append("All requests use `Content-Type: application/json; charset=UTF-8` and the same payload shape: id, userId, category, 40 numbers, details, and tags.\n\n");
        report.append("- `Request-1`: accepts JSON, parses it with the selected parser backend, writes a JSONL record to `3/runtime/store-<variant>.jsonl`, reads the last line from that file, and returns JSON with a checksum of the retrieved data.\n");
        report.append("- `Request-2`: accepts JSON, parses it with the selected parser backend, reads an item from a prefilled in-memory map with 1024 entries, performs calculations over the numeric array, and returns JSON.\n\n");

        report.append("## Hardware description\n\n");
        report.append("| Component | Value |\n");
        report.append("|-----------|-------|\n");
        report.append("| CPU | AMD Ryzen 5 5600H with Radeon Graphics, 3.30 GHz |\n");
        report.append("| Logical processors | 12 |\n");
        report.append("| RAM | 16.0 GB, 3200 MT/s |\n");
        report.append("| GPU | AMD Radeon(TM) Graphics, 496 MB |\n");
        report.append("| Disk | 477 GB |\n");
        report.append("| OS | ").append(System.getProperty("os.name")).append(' ').append(System.getProperty("os.version")).append(" |\n");
        report.append("| JDK | ").append(System.getProperty("java.version")).append(" |\n\n");

        report.append("## Experiment parameters\n\n");
        report.append("| Parameter | Value |\n");
        report.append("|-----------|-------|\n");
        report.append("| Host | `").append(config.host()).append("` |\n");
        report.append("| Base port | `").append(config.basePort()).append("` |\n");
        report.append("| Server threads | `").append(config.serverThreads()).append("` |\n");
        report.append("| Client threads | `").append(config.clientThreads()).append("` |\n");
        report.append("| Warmup requests | `").append(config.warmupRequests()).append("` per endpoint/variant |\n");
        report.append("| Measured requests | `").append(config.requests()).append("` per repeat |\n");
        report.append("| Repeats | `").append(config.repeats()).append("` |\n");
        report.append("| Payload numbers | `40` numeric values per request |\n");
        report.append("| Load generator | custom Java client based on `java.net.http.HttpClient` |\n\n");

        report.append("## Resulting table\n\n");
        report.append("Average time per request, milliseconds. Values are averaged across repeats.\n\n");
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
        report.append("Generated at: `").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("`.\n");
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
}
