package tokenizer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonMapper {
    public static <T> T fromMap (Map<String,Object> map, Class<T> clas) {
        try {
            T obj = clas.getDeclaredConstructor().newInstance();

            for (var field : clas.getDeclaredFields()) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object value = map.get(fieldName);

                if (value == null) {
                    continue;
                }

                if (value instanceof Map) {
                    Object nestedObject = fromMap((Map<String, Object>) value, field.getType());
                    field.set(obj, nestedObject);
                } else if (value instanceof List) {
                    List<?> sourceList = (List<?>) value;
                    List<Object> targetList = new ArrayList<>();

                    Type genericType = field.getGenericType();

                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
                        Type elementType = parameterizedType.getActualTypeArguments()[0];

                        if (elementType instanceof Class) {
                            Class<?> elementClass = (Class<?>) elementType;

                            for (Object item : sourceList) {
                                if (item instanceof Map) {
                                    Object nestedItem = fromMap((Map<String, Object>) item, elementClass);
                                    targetList.add(nestedItem);
                                } else {
                                    targetList.add(item);
                                }
                            }
                        } else {
                            targetList.addAll(sourceList);
                        }
                    } else {
                        targetList.addAll(sourceList);
                    }

                    field.set(obj, targetList);
                } else {
                    field.set(obj, value);
                }

            }

            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
