package loadtest;

final class JsonCodecs {
    private JsonCodecs() {
    }

    static JsonCodec create(JsonBackend backend) {
        return switch (backend) {
            case OWN -> new OwnJsonCodec();
            case GSON -> new GsonJsonCodec();
        };
    }
}
