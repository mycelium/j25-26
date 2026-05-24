package loadtesting;

/**
 * Describes one test configuration: thread model and JSON library choice.
 */
public record BenchmarkConfig(String name, boolean virtualThreads, boolean useOwnJson) {

    public static BenchmarkConfig[] all() {
        return new BenchmarkConfig[] {
                new BenchmarkConfig("Virtual  + Own JSON", true,  true),
                new BenchmarkConfig("Virtual  + Gson",     true,  false),
                new BenchmarkConfig("Classic  + Own JSON", false, true),
                new BenchmarkConfig("Classic  + Gson",     false, false),
        };
    }
}