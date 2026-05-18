package org.example.loadtest;

import org.example.http.HttpServer;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        int serverPort = 18090;
        int serverThreads = 8;
        int clientThreads = 10;
        int requestsPerThread = 200;
        int warmupRequests = 20;

        // Parse CLI args
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--port" -> serverPort = Integer.parseInt(args[i + 1]);
                case "--server-threads" -> serverThreads = Integer.parseInt(args[i + 1]);
                case "--client-threads" -> clientThreads = Integer.parseInt(args[i + 1]);
                case "--requests" -> requestsPerThread = Integer.parseInt(args[i + 1]);
            }
        }

        System.out.println("=== HTTP Server + JSON Parser Load Test ===");
        System.out.println("Server threads: " + serverThreads);
        System.out.println("Client threads: " + clientThreads);
        System.out.println("Requests per thread: " + requestsPerThread);
        System.out.println("Total requests per endpoint: " + (clientThreads * requestsPerThread));
        System.out.println();

        Config[] configs = {
            new Config(true,  true,  serverThreads, clientThreads, requestsPerThread, serverPort),
            new Config(true,  false, serverThreads, clientThreads, requestsPerThread, serverPort),
            new Config(false, true,  serverThreads, clientThreads, requestsPerThread, serverPort),
            new Config(false, false, serverThreads, clientThreads, requestsPerThread, serverPort),
        };

        double[][] results = new double[2][4];

        for (int i = 0; i < configs.length; i++) {
            Config cfg = configs[i];
            System.out.println("--- Running: " + cfg.name() + " ---");

            // Clean data file before each run
            Files.deleteIfExists(Paths.get("load-test-data.txt"));

            HttpServer server = ServerApp.buildServer(cfg);
            server.start();
            Thread.sleep(300);

            try {
                // Warmup
                System.out.print("  Warming up... ");
                LoadTestRunner.run(cfg, "/request1", 2, warmupRequests);
                LoadTestRunner.run(cfg, "/request2", 2, warmupRequests);
                System.out.println("done");

                // Actual test
                System.out.print("  Testing /request1... ");
                results[0][i] = LoadTestRunner.run(cfg, "/request1", clientThreads, requestsPerThread);
                System.out.printf("%.3f ms avg%n", results[0][i]);

                System.out.print("  Testing /request2... ");
                results[1][i] = LoadTestRunner.run(cfg, "/request2", clientThreads, requestsPerThread);
                System.out.printf("%.3f ms avg%n", results[1][i]);

            } finally {
                server.stop();
                Thread.sleep(300);
            }
            System.out.println();
        }

        // Print results table
        printTable(results, configs);
    }

    private static void printTable(double[][] results, Config[] configs) {
        System.out.println("=== RESULTS TABLE (avg ms per request) ===");
        System.out.println();

        String[] headers = {"req", "Virtual + own parser", "Virtual + GSON", "Classic + own parser", "Classic + GSON"};
        int[] widths = {12, 22, 16, 22, 16};

        printRow(headers, widths);
        printSeparator(widths);

        String[] reqNames = {"Request-1", "Request-2"};
        for (int r = 0; r < 2; r++) {
            String[] row = new String[5];
            row[0] = reqNames[r];
            for (int c = 0; c < 4; c++) {
                row[c + 1] = String.format("%.3f ms", results[r][c]);
            }
            printRow(row, widths);
        }
        System.out.println();
    }

    private static void printRow(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.length; i++) {
            sb.append(String.format(" %-" + (widths[i] - 1) + "s", cells[i])).append("|");
        }
        System.out.println(sb);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int w : widths) {
            sb.append("-".repeat(w)).append("|");
        }
        System.out.println(sb);
    }
}
