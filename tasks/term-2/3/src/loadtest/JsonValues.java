package loadtest;

import java.math.BigDecimal;
import java.util.List;

final class JsonValues {
    private JsonValues() {
    }

    static String stringValue(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    static long longValue(Object value, long defaultValue) {
        if (value instanceof BigDecimal decimal) {
            return decimal.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            return Long.parseLong(string);
        }
        return defaultValue;
    }

    static List<?> list(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return List.of();
    }
}
