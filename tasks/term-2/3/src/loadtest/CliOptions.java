package loadtest;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class CliOptions {
    private final Map<String, String> values;

    private CliOptions(Map<String, String> values) {
        this.values = values;
    }

    static CliOptions parse(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }

            String key = arg.substring(2);
            if (key.isBlank()) {
                throw new IllegalArgumentException("Empty option name");
            }

            if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                values.put(key, args[++i]);
            } else {
                values.put(key, "true");
            }
        }
        return new CliOptions(values);
    }

    String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    int getInt(String key, int defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    long getLong(String key, long defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    boolean getBoolean(String key, boolean defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    Path getPath(String key, Path defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : Path.of(value);
    }

    boolean has(String key) {
        return values.containsKey(key);
    }
}
