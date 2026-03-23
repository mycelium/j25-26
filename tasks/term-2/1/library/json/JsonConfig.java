package json;

public final class JsonConfig {
    private final boolean failOnUnknownProperties;
    private final boolean includeNullFields;
    private final boolean detectCycles;

    private JsonConfig(Builder builder) {
        this.failOnUnknownProperties = builder.failOnUnknownProperties;
        this.includeNullFields = builder.includeNullFields;
        this.detectCycles = builder.detectCycles;
    }

    public static JsonConfig defaultConfig() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isFailOnUnknownProperties() {
        return failOnUnknownProperties;
    }

    public boolean isIncludeNullFields() {
        return includeNullFields;
    }

    public boolean isDetectCycles() {
        return detectCycles;
    }

    public static final class Builder {
        private boolean failOnUnknownProperties;
        private boolean includeNullFields = true;
        private boolean detectCycles = true;

        private Builder() {
        }

        public Builder failOnUnknownProperties(boolean value) {
            this.failOnUnknownProperties = value;
            return this;
        }

        public Builder includeNullFields(boolean value) {
            this.includeNullFields = value;
            return this;
        }

        public Builder detectCycles(boolean value) {
            this.detectCycles = value;
            return this;
        }

        public JsonConfig build() {
            return new JsonConfig(this);
        }
    }
}
