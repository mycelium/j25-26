package loadtest;

enum BenchmarkEndpoint {
    REQUEST_1("Request-1", "/request-1"),
    REQUEST_2("Request-2", "/request-2");

    private final String displayName;
    private final String path;

    BenchmarkEndpoint(String displayName, String path) {
        this.displayName = displayName;
        this.path = path;
    }

    String displayName() {
        return displayName;
    }

    String path() {
        return path;
    }

    static BenchmarkEndpoint fromValue(String value) {
        for (BenchmarkEndpoint endpoint : values()) {
            if (endpoint.displayName.equalsIgnoreCase(value)
                    || endpoint.path.equalsIgnoreCase(value)
                    || endpoint.name().equalsIgnoreCase(value.replace('-', '_'))) {
                return endpoint;
            }
        }
        throw new IllegalArgumentException("Unknown endpoint: " + value);
    }
}
