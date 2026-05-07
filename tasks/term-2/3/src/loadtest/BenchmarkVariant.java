package loadtest;

enum BenchmarkVariant {
    VIRTUAL_OWN("Virtual + own parser", "virtual-own", true, JsonBackend.OWN),
    VIRTUAL_GSON("Virtual + GSON", "virtual-gson", true, JsonBackend.GSON),
    CLASSIC_OWN("Classic + own parser", "classic-own", false, JsonBackend.OWN),
    CLASSIC_GSON("Classic + GSON", "classic-gson", false, JsonBackend.GSON);

    private final String displayName;
    private final String fileName;
    private final boolean isVirtual;
    private final JsonBackend backend;

    BenchmarkVariant(String displayName, String fileName, boolean isVirtual, JsonBackend backend) {
        this.displayName = displayName;
        this.fileName = fileName;
        this.isVirtual = isVirtual;
        this.backend = backend;
    }

    String displayName() {
        return displayName;
    }

    String fileName() {
        return fileName;
    }

    boolean isVirtual() {
        return isVirtual;
    }

    JsonBackend backend() {
        return backend;
    }

    static BenchmarkVariant fromValue(String value) {
        String normalized = value.toLowerCase().replace('_', '-').replace(' ', '-');
        for (BenchmarkVariant variant : values()) {
            if (variant.fileName.equals(normalized) || variant.name().toLowerCase().replace('_', '-').equals(normalized)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown variant: " + value);
    }

    static BenchmarkVariant fromParts(boolean isVirtual, JsonBackend backend) {
        for (BenchmarkVariant variant : values()) {
            if (variant.isVirtual == isVirtual && variant.backend == backend) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unsupported variant");
    }
}
