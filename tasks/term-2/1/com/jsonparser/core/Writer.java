package com.jsonparser.core;

import com.jsonparser.exception.JsonException;
import java.lang.reflect.*;
import java.util.*;

public class Writer {

    public String write(Object obj) {
        if (obj == null) return "null";
        StringBuilder sb = new StringBuilder();
        writeValue(obj, sb);
        return sb.toString();
    }

    private void writeValue(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
            return;
        }

        switch (obj) {
            case String s -> sb.append('"').append(escape(s)).append('"');
            case Character c -> sb.append('"').append(escape(c.toString())).append('"');
            case Number n -> sb.append(obj);
            case Boolean b -> sb.append(obj);
            default -> {
                Class<?> cls = obj.getClass();
                if (cls.isArray()) {
                    writeArray(obj, sb);
                } else if (obj instanceof Collection<?> coll) {
                    writeCollection(coll, sb);
                } else if (obj instanceof Map<?, ?> map) {
                    writeMap(map, sb);
                } else {
                    writeObject(obj, sb);
                }
            }
        }
    }

    private void writeArray(Object arr, StringBuilder sb) {
        int len = Array.getLength(arr);
        sb.append("[");
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(",");
            writeValue(Array.get(arr, i), sb);
        }
        sb.append("]");
    }

    private void writeCollection(Collection<?> coll, StringBuilder sb) {
        sb.append("[");
        int i = 0;
        for (Object item : coll) {
            if (i++ > 0) sb.append(",");
            writeValue(item, sb);
        }
        sb.append("]");
    }

    private void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append('"').append(escape(String.valueOf(entry.getKey()))).append('"');
            sb.append(":");
            writeValue(entry.getValue(), sb);
        }
        sb.append("}");
    }

    private void writeObject(Object obj, StringBuilder sb) {
        Field[] fields = obj.getClass().getDeclaredFields();
        sb.append("{");
        boolean first = true;

        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            f.setAccessible(true);
            try {
                Object val = f.get(obj);
                if (!first) sb.append(",");
                sb.append('"').append(escape(f.getName())).append('"');
                sb.append(":");
                writeValue(val, sb);
                first = false;
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot access field: " + f.getName(), e);
            }
        }
        sb.append("}");
    }

    private String escape(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}