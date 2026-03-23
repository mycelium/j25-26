package json;

import json.exceptions.JsonMappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ReflectionUtils {
    private ReflectionUtils() {
    }

    static Map<String, Field> getMappableFields(Class<?> targetClass) {
        Map<String, Field> fieldsByName = new LinkedHashMap<>();
        Class<?> current = targetClass;
        while (current != null && current != Object.class) {
            Field[] declaredFields = current.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!isSupportedField(field)) {
                    continue;
                }
                field.setAccessible(true);
                fieldsByName.putIfAbsent(field.getName(), field);
            }
            current = current.getSuperclass();
        }
        return fieldsByName;
    }

    static List<Field> getSerializableFields(Class<?> targetClass) {
        List<Field> result = new ArrayList<>(getMappableFields(targetClass).values());
        result.sort(Comparator.comparing(Field::getName));
        return result;
    }

    static <T> T newInstance(Class<T> targetClass) {
        if (targetClass.isInterface()) {
            throw new JsonMappingException("Cannot instantiate interface: " + targetClass.getName());
        }
        if (Modifier.isAbstract(targetClass.getModifiers())) {
            throw new JsonMappingException("Cannot instantiate abstract class: " + targetClass.getName());
        }
        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new JsonMappingException("Cannot instantiate " + targetClass.getName() + ": no accessible no-arg constructor", ex);
        }
    }

    private static boolean isSupportedField(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !field.isSynthetic();
    }
}
