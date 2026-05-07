package loadtest;

final class BenchmarkDefaults {
    static final String HOST = "127.0.0.1";
    static final int BASE_PORT = 18080;
    static final int SERVER_THREADS = 12;
    static final int CLIENT_THREADS = 64;
    static final int WARMUP_REQUESTS = 500;
    static final int REQUESTS = 5000;
    static final int REPEATS = 3;
    static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    private BenchmarkDefaults() {
    }
}
