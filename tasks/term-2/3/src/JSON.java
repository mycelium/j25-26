import java.lang.reflect.*;
import java.util.*;

public class JSON {

	private JSON() {
	} // private - чтобы не создавать экземпляры

	private static Object serialize(Object obj) { // любой obj в json
		if (obj == null)
			return "null";
		if (obj instanceof String)
			return "\"" + escape((String) obj) + "\"";
		if (obj instanceof Number || obj instanceof Boolean)
			return obj.toString();
		if (obj instanceof Collection)
			return serializeCollection((Collection<?>) obj);
		if (obj.getClass().isArray())
			return serializeArray(obj);
		if (obj instanceof Map)
			return serializeMap((Map<?, ?>) obj);
		return serializeObject(obj);
	}

	private static String serializeCollection(Collection<?> col) {
		List<String> items = new ArrayList<>();
		for (Object item : col) {
			items.add(serialize(item).toString());
		}
		return "[" + String.join(",", items) + "]";
	}

	private static String serializeArray(Object arr) {
		List<String> items = new ArrayList<>();
		int len = Array.getLength(arr);
		for (int i = 0; i < len; i++) {
			items.add(serialize(Array.get(arr, i)).toString());
		}
		return "[" + String.join(",", items) + "]";
	}

	private static String serializeMap(Map<?, ?> map) {
		List<String> entries = new ArrayList<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String key = "\"" + entry.getKey().toString() + "\"";
			String value = serialize(entry.getValue()).toString();
			entries.add(key + ":" + value);
		}
		return "{" + String.join(",", entries) + "}";
	}

	private static String serializeObject(Object obj) {
		List<String> fields = new ArrayList<>();
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()))
				continue;

			field.setAccessible(true);
			try {
				Object value = field.get(obj);
				String name = "\"" + field.getName() + "\"";
				fields.add(name + ":" + serialize(value));
			} catch (IllegalAccessException e) {
				// игнорируем
			}
		}
		return "{" + String.join(",", fields) + "}";
	}

	private static String escape(String s) { // экранируем спец символы
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

	public static String toJson(Object obj) {
		if (obj == null)
			return "null";
		return serialize(obj).toString();
	}

	public static <T> T parse(String json, Class<T> с) { // json в объект
		Map<String, Object> map = parseToMap(json);
		return mapToObject(map, с);
	}

	public static Map<String, Object> parseToMap(String json) { // json в map
		return (Map<String, Object>) new Parser(json).parse();
	}

	private static class Parser {
		private final String input; // json
		private int pos = 0; // позиция в строке

		Parser(String input) {
			this.input = input.trim(); // убираем лишние пробелы
		}

		Object parse() {
			skip(); // пропуск пробелов

			if (pos >= input.length())
				return null;

			char c = input.charAt(pos); // определяем принадлежность
			if (c == '{')
				return parseObject();
			if (c == '[')
				return parseArray();
			if (c == '"')
				return parseString();
			if (c == 't' || c == 'f')
				return parseBoolean();
			if (c == 'n')
				return parseNull();
			return parseNumber();
		}

		private Map<String, Object> parseObject() {
			Map<String, Object> map = new HashMap<>();
			pos++; // скип {

			skip();
			if (pos < input.length() && input.charAt(pos) == '}') { // пустой obj
				pos++;
				return map;
			}

			while (true) {
				skip();
				if (input.charAt(pos) != '"')
					throw new RuntimeException("Expected '\"' at " + pos);
				String key = parseString();

				skip();
				if (input.charAt(pos) != ':')
					throw new RuntimeException("Expected ':' at " + pos);
				pos++;

				skip();
				Object value = parse();
				map.put(key, value);

				skip();
				if (pos >= input.length())
					break;

				char c = input.charAt(pos);
				if (c == '}') { // конец
					pos++;
					break;
				}
				if (c == ',') { // не конец
					pos++;
					continue;
				}
			}
			return map;
		}

		private List<Object> parseArray() {
			List<Object> list = new ArrayList<>();
			pos++; // пропускаем [

			skip();
			if (pos < input.length() && input.charAt(pos) == ']') {
				pos++;
				return list;
			}

			while (true) {
				skip();
				list.add(parse());

				skip();
				if (pos >= input.length())
					break;

				char c = input.charAt(pos);
				if (c == ']') {
					pos++;
					break;
				}
				if (c == ',') {
					pos++;
					continue;
				}
			}
			return list;
		}

		private String parseString() {
			pos++; // пропускаем "
			StringBuilder sb = new StringBuilder();

			while (pos < input.length()) {
				char c = input.charAt(pos);
				if (c == '"') {
					pos++;
					break;
				}
				if (c == '\\') {
					pos++;
					c = input.charAt(pos);
					switch (c) {
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 't':
						sb.append('\t');
						break;
					default:
						sb.append(c);
					}
				} else {
					sb.append(c);
				}
				pos++;
			}
			return sb.toString();
		}

		private Number parseNumber() {
			int start = pos;
			while (pos < input.length()) {
				char c = input.charAt(pos);
				if (c == ',' || c == ']' || c == '}' || Character.isWhitespace(c)) {
					break;
				}
				pos++;
			}
			String num = input.substring(start, pos);
			if (num.contains(".")) {
				return Double.parseDouble(num);
			}
			return Long.parseLong(num);
		}

		private Boolean parseBoolean() {
			if (input.startsWith("true", pos)) {
				pos += 4;
				return true;
			}
			if (input.startsWith("false", pos)) {
				pos += 5;
				return false;
			}
			throw new RuntimeException("Expected boolean at " + pos);
		}

		private Object parseNull() {
			if (input.startsWith("null", pos)) {
				pos += 4;
				return null;
			}
			throw new RuntimeException("Expected null at " + pos);
		}

		private void skip() {
			while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
				pos++;
			}
		}
	}

	private static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
		try {
			T obj = clazz.getDeclaredConstructor().newInstance();

			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;

				String name = field.getName();
				if (!map.containsKey(name))
					continue;

				Object value = map.get(name);
				field.setAccessible(true);

				// если значение null - устанавливаем null
				if (value == null) {
					field.set(obj, null);
					continue;
				}

				Class<?> fieldType = field.getType();

				// примитивы и обертки
				if (fieldType == int.class || fieldType == Integer.class) {
					field.set(obj, ((Number) value).intValue());
				} else if (fieldType == long.class || fieldType == Long.class) {
					field.set(obj, ((Number) value).longValue());
				} else if (fieldType == double.class || fieldType == Double.class) {
					field.set(obj, ((Number) value).doubleValue());
				} else if (fieldType == float.class || fieldType == Float.class) {
					field.set(obj, ((Number) value).floatValue());
				} else if (fieldType == boolean.class || fieldType == Boolean.class) {
					field.set(obj, value);
				} else if (fieldType == String.class) {
					field.set(obj, value.toString());
				}
				// вложенные объекты
				else if (value instanceof Map) {
					Object nestedObj = mapToObject((Map<String, Object>) value, fieldType);
					field.set(obj, nestedObj);
				}
				// коллекции
				else if (List.class.isAssignableFrom(fieldType)) {
					field.set(obj, value);
				}
				// массивы
				else if (fieldType.isArray()) {
					field.set(obj, value);
				} else {
					field.set(obj, value);
				}
			}
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to create " + clazz.getSimpleName() + ": " + e.getMessage());
		}
	}
}