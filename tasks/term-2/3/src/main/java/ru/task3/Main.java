package ru.task3;

public class Main {

    public static void main(String[] args)
            throws Exception {

        JsonAdapter json = new OwnJsonAdapter();

        //JsonAdapter json = new GsonJsonAdapter();

        TestServer server =
                new TestServer(
                        true,
                        json
                );

        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);

        BenchmarkRunner.run(
                "Request-1",
                "/request1",
                1000,
                8
        );

        BenchmarkRunner.run(
                "Request-2",
                "/request2",
                1000,
                8
        );
    }
}
