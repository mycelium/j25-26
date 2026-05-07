package loadtest;

enum JsonBackend {
    OWN,
    GSON;

    static JsonBackend fromValue(String value) {
        String normalized = value.toLowerCase();
        return switch (normalized) {
            case "own", "own-parser", "custom" -> OWN;
            case "gson" -> GSON;
            default -> throw new IllegalArgumentException("Unknown JSON backend: " + value);
        };
    }
}
