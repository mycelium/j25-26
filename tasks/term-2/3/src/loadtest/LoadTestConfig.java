package loadtest;

/**
 * Immutable configuration for one load-test scenario.
 */
public final class LoadTestConfig {

    public final String  scenarioName;
    public final boolean virtualThreads;
    public final JsonAdapter jsonAdapter;
    public final int     threadCount;
    public final int     totalRequests;
    public final String  targetHost;
    public final int     targetPort;

    private LoadTestConfig(Builder b) {
        this.scenarioName   = b.scenarioName;
        this.virtualThreads = b.virtualThreads;
        this.jsonAdapter    = b.jsonAdapter;
        this.threadCount    = b.threadCount;
        this.totalRequests  = b.totalRequests;
        this.targetHost     = b.targetHost;
        this.targetPort     = b.targetPort;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String      scenarioName   = "Unnamed";
        private boolean     virtualThreads = false;
        private JsonAdapter jsonAdapter    = JsonAdapter.OWN;
        private int         threadCount    = 10;
        private int         totalRequests  = 1000;
        private String      targetHost     = "127.0.0.1";
        private int         targetPort     = 8080;

        public Builder scenarioName(String v)   { scenarioName   = v; return this; }
        public Builder virtualThreads(boolean v){ virtualThreads = v; return this; }
        public Builder jsonAdapter(JsonAdapter v){ jsonAdapter     = v; return this; }
        public Builder threadCount(int v)        { threadCount    = v; return this; }
        public Builder totalRequests(int v)      { totalRequests  = v; return this; }
        public Builder targetHost(String v)      { targetHost     = v; return this; }
        public Builder targetPort(int v)         { targetPort     = v; return this; }

        public LoadTestConfig build() { return new LoadTestConfig(this); }
    }

    @Override
    public String toString() {
        return String.format("%-35s | threads=%-4d | virtual=%-5s | parser=%s | requests=%d",
            scenarioName, threadCount, virtualThreads, jsonAdapter, totalRequests);
    }
}
