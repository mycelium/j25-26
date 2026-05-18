package jsonlib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class ClassInfo {

    // cache key = (Class, FieldNamingStrategy)
    private static final ConcurrentHashMap<Long, ClassInfo> CACHE =
        new ConcurrentHashMap<>();

    /** No-arg constructor, already accessible. */
    final Constructor<?> constructor;

    
    final Map<String, Field> fieldsByJsonKey;

    
    static ClassInfo of(Class<?> clazz, FieldNamingStrategy strategy) {
        // Cheap composite key: identity hash of class + ordinal of strategy
        long key = ((long) System.identityHashCode(clazz) << 32)
                 | (strategy.ordinal() & 0xFFFFFFFFL);

        return CACHE.computeIfAbsent(key, k -> build(clazz, strategy));
    }

    
    private static ClassInfo build(Class<?> clazz, FieldNamingStrategy strategy) {
        // 1. No-arg constructor
        Constructor<?> ctor;
        try {
            ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new JsonParseException(
                clazz.getName() + " has no no-arg constructor. "
                + "Add one (can be private) so the mapper can instantiate it."
            );
        }

        // 2. Walk the inheritance chain — parent fields first
        List<Class<?>> hierarchy = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            hierarchy.add(0, c); // prepend so parent comes first
        }

        Map<String, Field> byKey = new LinkedHashMap<>();
        for (Class<?> c : hierarchy) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.isSynthetic()) continue;
                f.setAccessible(true);
                String jsonKey = strategy.toJsonKey(f.getName());
                byKey.put(jsonKey, f);
            }
        }

        return new ClassInfo(ctor, Collections.unmodifiableMap(byKey));
    }

   
    private ClassInfo(Constructor<?> constructor, Map<String, Field> fieldsByJsonKey) {
        this.constructor    = constructor;
        this.fieldsByJsonKey = fieldsByJsonKey;
    }

    /** Reverse lookup: Java field name → Field object (for serialization). */
    Collection<Field> fields() {
        return fieldsByJsonKey.values();
    }
}
