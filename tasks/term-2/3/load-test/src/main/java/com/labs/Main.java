package com.labs;

import com.labs.config.*;
import com.labs.server.TestServer;
import com.labs.client.LoadTester;
import com.labs.client.TestResult;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Запуск нагрузочного тестирования...\n");

        var configs = new ConfigEntry[] {
                new ConfigEntry(ThreadMode.VIRTUAL, ParserMode.OWN, "Virtual + Own Parser"),
                new ConfigEntry(ThreadMode.VIRTUAL, ParserMode.GSON, "Virtual + GSON"),
                new ConfigEntry(ThreadMode.CLASSIC, ParserMode.OWN, "Classic + Own Parser"),
                new ConfigEntry(ThreadMode.CLASSIC, ParserMode.GSON, "Classic + GSON")
        };

        System.out.println("| Запрос | " +
                String.join(" | ",
                        configs[0].label, configs[1].label,
                        configs[2].label, configs[3].label) + " |");
        System.out.println("|--------|" +
                "----------------------|".repeat(4));

        for (String endpoint : new String[]{"/request1", "/request2"}) {
            String label = endpoint.equals("/request1") ? "Request-1" : "Request-2";
            System.out.print("| " + label + " | ");

            for (var cfg : configs) {
                TestResult result = runSingleTest(cfg, endpoint);
                System.out.printf("%.2f ms | ", result.avgTimeMs);
            }
            System.out.println();
        }
    }

    private static TestResult runSingleTest(ConfigEntry cfg, String endpoint) {
        System.out.println("\n▶ Тест: " + cfg.label + " | " + endpoint);

        TestServer server = new TestServer(
                TestConfig.HOST,
                TestConfig.PORT,
                cfg.threadMode == ThreadMode.CLASSIC ? TestConfig.CLASSIC_THREAD_POOL : 0,
                cfg.threadMode == ThreadMode.VIRTUAL,
                cfg.parserMode
        );

        try {
            server.start();
            Thread.sleep(500);

            LoadTester tester = new LoadTester("http://" + TestConfig.HOST + ":" + TestConfig.PORT);
            String payload = endpoint.equals("/request1") ? TestConfig.PAYLOAD_REQ1 : TestConfig.PAYLOAD_REQ2;

            return tester.runTest(
                    endpoint,
                    payload,
                    TestConfig.CONCURRENCY,
                    TestConfig.REQUESTS_PER_THREAD
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new TestResult(endpoint, -1, 0, 0);
        } finally {
            server.stop();

            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
    }

    record ConfigEntry(ThreadMode threadMode, ParserMode parserMode, String label) {}
}