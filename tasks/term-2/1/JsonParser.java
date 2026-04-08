import java.lang.reflect.*;
import java.util.*;

public class JsonParser {

    public static Map<String, Object> parseToMap(String json) {
        return (Map<String, Object>) parseValue(json.trim());
    }

    public static <T> T parseToObject(String json, Class<T> targetClass) {
        Map<String, Object> map = parseToMap(json);
        return convertMapToObject(map, targetClass);
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        return serializeValue(obj);
    }


    private static Object parseValue(String json) {
        json = json.trim();

        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"")) {
            return parseString(json);
        } else if (json.equals("null")) {
            return null;
        } else if (json.equals("true") || json.equals("false")) {
            return Boolean.parseBoolean(json);
        } else if (json.matches("-?\\d+(\\.\\d+)?")) {
            if (json.contains(".")) {
                return Double.parseDouble(json);
            } else {
                // проверяем, помещается ли в Integer
                long longVal = Long.parseLong(json);
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        }

        return json;
    }

    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim();

        if (json.startsWith("{")) {
            json = json.substring(1);
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1);
        }

        json = json.trim();
        if (json.isEmpty()) {
            return map;
        }

        String[] pairs = splitJsonObject(json);

        for (String pair : pairs) {
            int colonIndex = findColonOutsideQuotes(pair);
            if (colonIndex == -1) continue;

            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();

            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }

            map.put(key, parseValue(value));
        }

        return map;
    }

    private static List<Object> parseArray(String json) {
        List<Object> list = new ArrayList<>();
        json = json.trim();

        if (json.startsWith("[")) {
            json = json.substring(1);
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }

        json = json.trim();
        if (json.isEmpty()) {
            return list;
        }

        String[] elements = splitJsonArray(json);

        for (String element : elements) {
            list.add(parseValue(element.trim()));
        }

        return list;
    }

    private static String parseString(String json) {
        json = json.trim();
        if (json.startsWith("\"") && json.endsWith("\"")) {
            return json.substring(1, json.length() - 1);
        }
        return json;
    }

    //  разбиение JSON объекта на пары
    private static String[] splitJsonObject(String json) {
        List<String> pairs = new ArrayList<>();
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuotes = false;
        int start = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '[') bracketCount++;
                if (c == ']') bracketCount--;

                if (c == ',' && braceCount == 0 && bracketCount == 0) {
                    pairs.add(json.substring(start, i));
                    start = i + 1;
                }
            }
        }

        if (start < json.length()) {
            pairs.add(json.substring(start));
        }

        return pairs.toArray(new String[0]);
    }

    //  разбиение JSON массива на элементы
    private static String[] splitJsonArray(String json) {
        List<String> elements = new ArrayList<>();
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuotes = false;
        int start = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '[') bracketCount++;
                if (c == ']') bracketCount--;

                if (c == ',' && braceCount == 0 && bracketCount == 0) {
                    elements.add(json.substring(start, i));
                    start = i + 1;
                }
            }
        }

        if (start < json.length()) {
            elements.add(json.substring(start));
        }

        return elements.toArray(new String[0]);
    }

    private static int findColonOutsideQuotes(String str) {
        boolean inQuotes = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"' && (i == 0 || str.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes && c == ':') {
                return i;
            }
        }
        return -1;
    }


    private static <T> T convertMapToObject(Map<String, Object> map, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();

            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (map.containsKey(fieldName)) {
                    Object value = map.get(fieldName);
                    Object convertedValue = convertValueToFieldType(value, field.getType());
                    field.set(instance, convertedValue);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error converting to " + targetClass.getSimpleName(), e);
        }
    }

    private static Object convertValueToFieldType(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }

        if (targetType.isArray()) {
            List<?> list = (List<?>) value;
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());

            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValueToFieldType(list.get(i), componentType));
            }
            return array;
        }

        if (targetType == List.class || targetType == ArrayList.class) {
            List<?> sourceList = (List<?>) value;
            List<Object> resultList = new ArrayList<>();
            for (Object item : sourceList) {
                if (item instanceof Map) {
                    resultList.add(convertMapToObject((Map<String, Object>) item, HashMap.class));
                } else {
                    resultList.add(item);
                }
            }
            return resultList;
        }

        if (value instanceof Map) {
            return convertMapToObject((Map<String, Object>) value, targetType);
        }

        return value;
    }


    private static String serializeValue(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj.getClass().isArray()) {
            return serializeArray(obj);
        }

        if (obj instanceof Collection) {
            return serializeCollection((Collection<?>) obj);
        }

        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }

        return serializeObject(obj);
    }

    private static String serializeArray(Object array) {
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            sb.append(serializeValue(Array.get(array, i)));
        }

        sb.append("]");
        return sb.toString();
    }

    private static String serializeCollection(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Object item : collection) {
            if (i > 0) sb.append(",");
            sb.append(serializeValue(item));
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(serializeValue(entry.getValue()));
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            try {
                Object value = field.get(obj);
                if (i > 0) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":");
                sb.append(serializeValue(value));
            } catch (IllegalAccessException e) {
                // пропускаем поле, если не можем получить доступ
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}