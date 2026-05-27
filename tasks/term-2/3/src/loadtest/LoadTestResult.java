package loadtest;

import java.util.List;

/**
 * Holds the result of a single load-test scenario for one request type.
 */
public final class LoadTestResult {

    public final String  scenarioName;
    public final String  requestLabel;   // "Request-1" or "Request-2"
    public final int     totalRequests;
    public final int     successCount;
    public final int     errorCount;
    public final long    totalMs;        // wall-clock duration
    public final double  avgMs;          // average latency per request
    public final double  p95Ms;         // 95th percentile latency
    public final double  throughput;    // requests / second

    public LoadTestResult(String scenarioName, String requestLabel,
                          int totalRequests, int successCount, int errorCount,
                          long totalMs, double avgMs, double p95Ms) {
        this.scenarioName  = scenarioName;
        this.requestLabel  = requestLabel;
        this.totalRequests = totalRequests;
        this.successCount  = successCount;
        this.errorCount    = errorCount;
        this.totalMs       = totalMs;
        this.avgMs         = avgMs;
        this.p95Ms         = p95Ms;
        this.throughput    = totalRequests / (totalMs / 1000.0);
    }

    /** One-line summary for console output. */
    public String summary() {
        return String.format(
            "%-12s | avg=%6.1f ms | p95=%6.1f ms | throughput=%6.1f req/s | ok=%d err=%d",
            requestLabel, avgMs, p95Ms, throughput, successCount, errorCount);
    }

    /** Row formatted for the Markdown results table. */
    public String markdownRow(String col) {
        return String.format("| %-12s | %-30s | %6.1f ms avg | %6.1f ms p95 | %6.1f req/s |",
            requestLabel, col, avgMs, p95Ms, throughput);
    }
public static void printTable(List<LoadTestResult> results) {
    System.out.println("\n=== RESULTS TABLE ===");
    System.out.printf("%-15s %-12s %8s %8s %8s%n", 
        "Scenario", "Request", "Avg(ms)", "p95(ms)", "Req/s");
    System.out.println("-".repeat(60));
    for (LoadTestResult r : results) {
        System.out.printf("%-15s %-12s %8.1f %8.1f %8.1f%n",
            r.scenarioName, r.requestLabel, r.avgMs, r.p95Ms, r.throughput);
    }
}
}
