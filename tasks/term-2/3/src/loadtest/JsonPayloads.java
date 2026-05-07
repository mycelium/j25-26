package loadtest;

final class JsonPayloads {
    private static final String COMMENT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private JsonPayloads() {
    }

    static String createRequestBody(int sequence) {
        int userId = Math.floorMod(sequence * 17, 4096);
        int category = Math.floorMod(sequence, 16);
        StringBuilder json = new StringBuilder(1024);
        json.append('{');
        appendStringField(json, "requestId", "req-" + sequence);
        json.append(',');
        appendNumberField(json, "userId", userId);
        json.append(',');
        appendStringField(json, "category", "category-" + category);
        json.append(',');
        appendNumberField(json, "timestamp", 1_778_153_600_000L + sequence);
        json.append(',');
        json.append("\"numbers\":[");
        for (int i = 0; i < 40; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(Math.floorMod(sequence * 31 + i * 17, 1000));
        }
        json.append("],");
        json.append("\"details\":{");
        appendStringField(json, "source", "load-generator");
        json.append(',');
        appendNumberField(json, "priority", Math.floorMod(sequence, 5) + 1);
        json.append(',');
        json.append("\"active\":true,");
        appendStringField(json, "comment", COMMENT + "-" + sequence);
        json.append("},");
        json.append("\"tags\":[\"java\",\"http\",\"json\",\"load-test\"]");
        json.append('}');
        return json.toString();
    }

    private static void appendStringField(StringBuilder json, String name, String value) {
        json.append('"').append(name).append("\":\"").append(escape(value)).append('"');
    }

    private static void appendNumberField(StringBuilder json, String name, long value) {
        json.append('"').append(name).append("\":").append(value);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
