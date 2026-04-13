package loadtesting;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    private static final String HOST              = "localhost";
    private static final int    PORT              = 9090;
    private static final int    TOTAL_REQUESTS    = 500;   // total requests per endpoint
    private static final int    CONCURRENT        = 50;    // parallel client threads
    private static final int    WARMUP_REQUESTS   = 50;    // warm-up before measuring

    private static final String BODY_REQUEST1 =
            "{\"value\": \"hello from load tester\"}";
    private static final String BODY_REQUEST2 =
            "{\"n\": 30}";

    record Config(String label, boolean isVirtual, boolean useOwnParser) {}

    private static final Config[] CONFIGS = {
            new Config("Virtual  + Own JSON", true,  true),
            new Config("Virtual  + Gson    ", true,  false),
            new Config("Classic  + Own JSON", false, true),
            new Config("Classic  + Gson    ", false, false),
    };

    public static void main(String[] args) throws Exception {

        Map<String, LoadTester.Result[]> results = new LinkedHashMap<>();

        for (Config cfg : CONFIGS) {
            System.out.println("\n========================================");
            System.out.println("Config: " + cfg.label());
            System.out.println("  isVirtual=" + cfg.isVirtual()
                    + "  useOwnParser=" + cfg.useOwnParser());
            System.out.println("========================================");

            ServerRunner runner = new ServerRunner(HOST, PORT, cfg.isVirtual(), cfg.useOwnParser());
            runner.start();
            Thread.sleep(300); // let the server bind

            try {
                String url1 = "http://" + HOST + ":" + PORT + "/request1";
                String url2 = "http://" + HOST + ":" + PORT + "/request2";

                System.out.println("[warm-up] /request1 ...");
                new LoadTester(WARMUP_REQUESTS, CONCURRENT, BODY_REQUEST1).run(url1);
                System.out.println("[warm-up] /request2 ...");
                new LoadTester(WARMUP_REQUESTS, CONCURRENT, BODY_REQUEST2).run(url2);

                System.out.println("[test]    /request1 ...");
                LoadTester.Result r1 = new LoadTester(TOTAL_REQUESTS, CONCURRENT, BODY_REQUEST1).run(url1);
                System.out.println("          " + r1);

                System.out.println("[test]    /request2 ...");
                LoadTester.Result r2 = new LoadTester(TOTAL_REQUESTS, CONCURRENT, BODY_REQUEST2).run(url2);
                System.out.println("          " + r2);

                results.put(cfg.label(), new LoadTester.Result[]{r1, r2});

            } finally {
                runner.stop();
                Thread.sleep(500); // let port free up before next config
            }
        }

        printTable(results);
    }

    private static void printTable(Map<String, LoadTester.Result[]> results) {
        System.out.println("\n");
        System.out.println("=".repeat(90));
        System.out.println("RESULTS  |  total requests: " + TOTAL_REQUESTS
                + "  |  concurrent: " + CONCURRENT);
        System.out.println("=".repeat(90));

        String header = String.format("%-10s | %-18s | %-18s | %-18s | %-18s",
                "req",
                "Virtual + own parser",
                "Virtual + Gson",
                "Classic + own parser",
                "Classic + Gson");
        System.out.println(header);
        System.out.println("-".repeat(90));

        String[] reqNames = {"Request-1", "Request-2"};

        for (int ep = 0; ep < 2; ep++) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%-10s |", reqNames[ep]));

            for (String label : results.keySet()) {
                LoadTester.Result r = results.get(label)[ep];
                row.append(String.format(" %-16d |", r.avgMs()));
            }

            System.out.println(row);
            System.out.println("-".repeat(90));
        }

        System.out.println("=".repeat(90));
        System.out.println("avg time per request is shown in milliseconds");
        System.out.println("Parameters: totalRequests=" + TOTAL_REQUESTS
                + "  concurrent=" + CONCURRENT
                + "  warmup=" + WARMUP_REQUESTS);
    }
}