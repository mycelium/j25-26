package ru.example.json;

import java.lang.reflect.*;
import java.util.*;

public class JsonSerializer {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String str) {
            return "\"" + str.replace("\"", "\\\"") + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();

        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            for (var entry : map.entrySet()) {
                sb.append(toJson(entry.getKey().toString()))
                        .append(":")
                        .append(toJson(entry.getValue()))
                        .append(",");
            }
            return trimComma(sb) + "}";
        }

        if (obj instanceof Collection<?> col) {
            StringBuilder sb = new StringBuilder("[");
            for (Object o : col) {
                sb.append(toJson(o)).append(",");
            }
            return trimComma(sb) + "]";
        }

        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int length = Array.getLength(obj);

            for (int i = 0; i < length; i++) {
                Object element = Array.get(obj, i);
                sb.append(toJson(element)).append(",");
            }

            return trimComma(sb) + "]";
        }

        StringBuilder sb = new StringBuilder("{");
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                sb.append(toJson(field.getName()))
                        .append(":")
                        .append(toJson(field.get(obj)))
                        .append(",");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return trimComma(sb) + "}";
    }

    private static String trimComma(StringBuilder sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
