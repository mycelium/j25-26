package loadtesting;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs load tests for all four configurations in sequence.
 * Each configuration starts the server, runs warm-up, measures two endpoints,
 * stops the server, then moves to the next.
 */
public class Main {

    private static final String HOST    = "localhost";
    private static final int    PORT    = 9090;

    // Test parameters
    private static final int TOTAL   = 500;
    private static final int THREADS = 50;
    private static final int WARMUP  = 50;

    private static final String BODY1 = "{\"value\": \"hello from load tester\"}";
    private static final String BODY2 = "{\"n\": 30}";

    public static void main(String[] args) throws Exception {
        Map<String, StatsCollector.Summary[]> table = new LinkedHashMap<>();

        for (BenchmarkConfig cfg : BenchmarkConfig.all()) {
            System.out.printf("%n=== %s ===%n", cfg.name());

            ServerRunner server = new ServerRunner(
                    HOST, PORT, cfg.virtualThreads(), cfg.useOwnJson());
            server.start();
            Thread.sleep(400);

            try {
                String url1 = "http://" + HOST + ":" + PORT + "/request1";
                String url2 = "http://" + HOST + ":" + PORT + "/request2";

                System.out.println("  [warm-up] /request1");
                new LoadTester(WARMUP, THREADS, BODY1).run(url1);
                System.out.println("  [warm-up] /request2");
                new LoadTester(WARMUP, THREADS, BODY2).run(url2);

                System.out.println("  [measure] /request1");
                StatsCollector.Summary s1 = new LoadTester(TOTAL, THREADS, BODY1).run(url1);
                System.out.println("            " + s1);

                System.out.println("  [measure] /request2");
                StatsCollector.Summary s2 = new LoadTester(TOTAL, THREADS, BODY2).run(url2);
                System.out.println("            " + s2);

                table.put(cfg.name(), new StatsCollector.Summary[]{s1, s2});

            } finally {
                server.stop();
                Thread.sleep(600);
            }
        }

        printTable(table);
    }

    private static void printTable(Map<String, StatsCollector.Summary[]> table) {
        int W = 90;
        System.out.println("\n\n" + "=".repeat(W));
        System.out.printf("RESULTS  —  requests: %d  |  concurrent: %d  |  warmup: %d%n",
                TOTAL, THREADS, WARMUP);
        System.out.println("=".repeat(W));

        System.out.printf("%-12s | %-18s | %-18s | %-18s | %-18s%n",
                "Endpoint",
                "Virtual + Own JSON",
                "Virtual + Gson",
                "Classic + Own JSON",
                "Classic + Gson");
        System.out.println("-".repeat(W));

        String[] endpoints = {"Request-1", "Request-2"};
        for (int i = 0; i < endpoints.length; i++) {
            StringBuilder row = new StringBuilder(String.format("%-12s |", endpoints[i]));
            for (StatsCollector.Summary[] summaries : table.values()) {
                row.append(String.format(" %-16s |", summaries[i].avgMs() + " ms"));
            }
            System.out.println(row);
            System.out.println("-".repeat(W));
        }

        System.out.println("avg latency per request in milliseconds");
    }
}