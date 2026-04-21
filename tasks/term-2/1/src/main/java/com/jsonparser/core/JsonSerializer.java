package com.jsonparser.core;

import com.jsonparser.exception.JsonParseException;
import java.lang.reflect.*;
import java.util.*;

/**
 * Преобразование Java объектов в JSON строку
 */
public class JsonSerializer {

    /**
     * Java объект в JSON строку
     */
    public String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        serializeValue(obj, sb);
        return sb.toString();
    }

    /**
     * Сериализация значения в зависимости от его типа
     */
    private void serializeValue(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
            return;
        }

        Class<?> clazz = obj.getClass();

        // Примитивы и строки
        if (obj instanceof String) {
            sb.append('"').append(escapeJson((String) obj)).append('"');
        } else if (obj instanceof Number) {
            sb.append(obj);
        } else if (obj instanceof Boolean) {
            sb.append(obj);
        } else if (clazz.isArray()) {
            serializeArray(obj, sb);
        } else if (obj instanceof Collection) {
            serializeCollection((Collection<?>) obj, sb);
        } else if (obj instanceof Map) {
            serializeMap((Map<?, ?>) obj, sb);
        } else {
            // Обычный Java объект
            serializeObject(obj, sb);
        }
    }

    /**
     * Сериализация массива
     */
    private void serializeArray(Object array, StringBuilder sb) {
        int length = Array.getLength(array);
        sb.append("[");

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            serializeValue(Array.get(array, i), sb);
        }

        sb.append("]");
    }

    /**
     * Сериализация коллекции
     */
    private void serializeCollection(Collection<?> collection, StringBuilder sb) {
        sb.append("[");
        int i = 0;
        for (Object item : collection) {
            if (i > 0) {
                sb.append(",");
            }
            serializeValue(item, sb);
            i++;
        }
        sb.append("]");
    }

    /**
     * Сериализация Map
     */
    private void serializeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append("{");
        int i = 0;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i > 0) {
                sb.append(",");
            }

            // Ключ всегда преобразуется в строку
            sb.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append('"');
            sb.append(":");
            serializeValue(entry.getValue(), sb);
            i++;
        }

        sb.append("}");
    }

    /**
     * Сериализация произвольного Java объекта
     */
    private void serializeObject(Object obj, StringBuilder sb) {
        Field[] fields = obj.getClass().getDeclaredFields();
        sb.append("{");

        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);

                if (!first) {
                    sb.append(",");
                }

                sb.append('"').append(field.getName()).append('"');
                sb.append(":");
                serializeValue(value, sb);

                first = false;
            } catch (IllegalAccessException e) {
                throw new JsonParseException("Ошибка доступа к полю: " + field.getName(), e);
            }
        }

        sb.append("}");
    }

    /**
     * Экранирование спецсимволов в JSON строке
     */
    private String escapeJson(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}