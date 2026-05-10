public final class JsonConfig {

    final boolean ignoreUnknownFields;
    final boolean serializeNulls;
    final boolean failOnDuplicateKeys;

    private JsonConfig(Builder b) {
        this.ignoreUnknownFields = b.ignoreUnknownFields;
        this.serializeNulls      = b.serializeNulls;
        this.failOnDuplicateKeys = b.failOnDuplicateKeys;
    }

    public static JsonConfig defaultConfig() {
        return builder().build();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean ignoreUnknownFields = false;
        private boolean serializeNulls      = false;
        private boolean failOnDuplicateKeys = true;

        public Builder ignoreUnknownFields(boolean val) {
            this.ignoreUnknownFields = val;
            return this;
        }

        public Builder serializeNulls(boolean val) {
            this.serializeNulls = val;
            return this;
        }

        public Builder failOnDuplicateKeys(boolean val) {
            this.failOnDuplicateKeys = val;
            return this;
        }

        public JsonConfig build() { return new JsonConfig(this); }
    }
}
